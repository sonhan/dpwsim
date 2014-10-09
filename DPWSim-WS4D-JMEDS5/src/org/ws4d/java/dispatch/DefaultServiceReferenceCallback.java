package org.ws4d.java.dispatch;

import org.ws4d.java.communication.CommunicationManager;
import org.ws4d.java.communication.DefaultResponseCallback;
import org.ws4d.java.communication.Discovery;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.ProtocolInfo;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.constants.DPWSMessageConstants;
import org.ws4d.java.dispatch.DefaultServiceReference.GetMetadataRequestSynchronizer;
import org.ws4d.java.dispatch.DefaultServiceReference.RequestSynchronizer;
import org.ws4d.java.dispatch.DefaultServiceReference.ResolveRequestSynchronizer;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.discovery.ResolveMatch;
import org.ws4d.java.message.discovery.ResolveMatchesMessage;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.message.metadata.GetMetadataResponseMessage;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.reference.Reference;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.EprInfo;
import org.ws4d.java.types.HostedMData;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.types.XAddressInfoSet;
import org.ws4d.java.util.Log;

public class DefaultServiceReferenceCallback extends DefaultResponseCallback {

	protected final DefaultServiceReference	servRef;

	/**
	 * @param servRef
	 */
	public DefaultServiceReferenceCallback(DefaultServiceReference servRef, XAddressInfo targetXAddressInfo) {
		super(targetXAddressInfo);
		this.servRef = servRef;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d.java
	 * .message.Message, org.ws4d.java.message.discovery.ResolveMatchesMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, ResolveMatchesMessage resolveMatches, ProtocolData protocolData) {
		ResolveRequestSynchronizer sync = null;
		try {
			synchronized (servRef) {
				sync = (ResolveRequestSynchronizer) servRef.synchronizers.remove(request.getMessageId());
				if (sync == null) {
					/*
					 * this shouldn't ever happen, as it would mean we receive a
					 * response to a request we never sent...
					 */
					Log.warn("Ignoring unexpected ResolveMatches message " + resolveMatches);
					return;
				}

				if (sync.hostedBlockVersion == servRef.hostedBlockVersion) {
					
					XAddressInfo targetXAddressInfo = getTargetAddress();
					if (targetXAddressInfo != null) {
						targetXAddressInfo.mergeProtocolInfo(protocolData.getProtocolInfo());
					}
					
					ResolveMatch match = resolveMatches.getResolveMatch();
					XAddressInfoSet xAddresses = match.getXAddressInfoSet();
					if (xAddresses != null) {
						EndpointReference epr = match.getEndpointReference();
						String comManId = protocolData.getCommunicationManagerId();
						OUTER: for (Iterator it = xAddresses.iterator(); it.hasNext();) {
							XAddressInfo xAdrInfo = (XAddressInfo) it.next();
							URI address = xAdrInfo.getXAddress();
							if (servRef.resolvedEprInfos == null) {
								servRef.resolvedEprInfos = new ArrayList(xAddresses.size());
								while (true) {
									if (protocolData.sourceMatches(address)) {
										servRef.resolvedEprInfos.add(0, new EprInfo(epr, address, comManId, protocolData.getProtocolInfo()));
									} else {
										servRef.resolvedEprInfos.add(new EprInfo(epr, address, comManId, protocolData.getProtocolInfo()));
									}
									if (!it.hasNext()) {
										break;
									}
									xAdrInfo = (XAddressInfo) it.next();
									address = xAdrInfo.getXAddress();
								}
								servRef.currentXAddressIndex = -1;
								break;
							}
							for (Iterator it2 = servRef.resolvedEprInfos.iterator(); it2.hasNext();) {
								EprInfo oldInfo = (EprInfo) it2.next();
								if (oldInfo.getXAddress().equals(address)) {
									continue OUTER;
								}
							}
							if (protocolData.sourceMatches(address)) {
								servRef.resolvedEprInfos.add(servRef.currentXAddressIndex, new EprInfo(epr, address, comManId, protocolData.getProtocolInfo()));
							} else {
								servRef.resolvedEprInfos.add(new EprInfo(epr, address, comManId, protocolData.getProtocolInfo()));
							}
						}
						if (servRef.resolvedEprInfos == null || servRef.currentXAddressIndex >= servRef.resolvedEprInfos.size() - 1) {
							if (maybeSendNextResolve(sync, protocolData.getProtocolInfo())) {
								return;
							}
						} else {
							sync.xAddress = servRef.preferredXAddressInfo = (EprInfo) servRef.resolvedEprInfos.get(++servRef.currentXAddressIndex);
						}
					}
				} else {
					if (Log.isDebug()) {
						Log.debug("Concurrent service update detected.", Log.DEBUG_LAYER_FRAMEWORK);
					}
				}

				if (sync == servRef.resolveSynchronizer) {
					servRef.resolveSynchronizer = null;
				}
			}
		} catch (Throwable e) {
			sync.exception = new TimeoutException("Unexpected exception during resolve matches processing: " + e);
		}

		synchronized (sync) {
			sync.pending = false;
			sync.notifyAll();
		}
	}

	private boolean maybeSendNextResolve(ResolveRequestSynchronizer sync, ProtocolInfo version) {
		if (servRef.unresolvedEPRs != null && servRef.unresolvedEPRs.size() > 0) {
			EndpointReference eprToResolve = (EndpointReference) servRef.unresolvedEPRs.remove(0);
			ResolveMessage resolve = new ResolveMessage(CommunicationManager.ID_NULL);
			servRef.synchronizers.put(resolve.getMessageId(), sync);
			resolve.setProtocolInfo(version);
			resolve.setEndpointReference(eprToResolve);
			OutDispatcher.getInstance().send(resolve, null, Discovery.getDefaultOutputDomains(), this);

			/*
			 * don't wake up waiters as result will come in later
			 */
			return true;
		} else {
			sync.exception = new TimeoutException("No more options to obtain transport address for service.");
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d.java
	 * .message.Message,
	 * org.ws4d.java.message.metadata.GetMetadataResponseMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, GetMetadataResponseMessage response, ProtocolData protocolData) {
		GetMetadataRequestSynchronizer sync = null;
		try {
			synchronized (servRef) {
				if (servRef.getLocation() == Reference.LOCATION_LOCAL) {
					Log.error("Received GetMetadataResponse message for a local reference");
					return;
				}
				servRef.setLocation(Reference.LOCATION_REMOTE);

				sync = (GetMetadataRequestSynchronizer) servRef.synchronizers.remove(request.getMessageId());
				if (sync == null) {
					/*
					 * this shouldn't ever happen, as it would mean we receive a
					 * response to a request we never sent...
					 */
					Log.warn("Ignoring unexpected GetMetadataResponse message " + response);
					return;
				}

				if (sync.hostedBlockVersion == servRef.hostedBlockVersion) {
					
					getTargetAddress().mergeProtocolInfo(protocolData.getProtocolInfo());
					
					/*
					 * set parent device ref
					 */
					if (response.getHost() != null) {
						EndpointReference devEpr = response.getHost().getEndpointReference();
						if (devEpr != null) {
							servRef.setParentDeviceReference(DeviceServiceRegistry.getDeviceReference(devEpr));
						}
					}
					
					/*
					 * update metadataReferences
					 */
					servRef.setMetadataReferences(response.getMetadataReferences());

					/*
					 * update metadata locations
					 */
					servRef.setMetaDataLocations(response.getMetadataLocations());

					/*
					 * update WSDLs
					 */
					servRef.setWSDLs(response.getWSDLs());
					
					HostedMData newHosted = response.getHosted(request.getTo());

					if (newHosted == null) {
						Service service = servRef.createProxyServiceFromLocalMetadata();
						if (service == null) {
							sync.exception = new TimeoutException("No Hosted block within GetMetadataResponse: " + response);
						} else {
							sync.service = service;
							Log.warn("Proxy service created from local metadata because no Hosted block was found within GetMetadataResponse: " + response);
						}
					} else {
						DeviceServiceRegistry.updateServiceReferenceRegistration(newHosted, servRef);

						// Do this before creating / updating proxy service
						servRef.setHostedFromService(newHosted, protocolData.getCommunicationManagerId(), protocolData);

						/*
						 * update / create proxy service, inform service
						 * listener
						 */
						try {
							servRef.checkAndUpdateService(protocolData);
						} catch (MissingMetadataException e) {
							sync.exception = new TimeoutException("Unable to create service proxy: " + e);
						}
					}
				} else {
					if (Log.isDebug()) {
						Log.debug("Concurrent service update detected, rebuilding service proxy", Log.DEBUG_LAYER_FRAMEWORK);
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			sync.exception = new TimeoutException("Unexpected exception during get metadata response processing: " + e);
		}

		synchronized (sync) {
			sync.pending = false;
			sync.notifyAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.DefaultResponseCallback#handle(org.ws4d.java
	 * .message.Message, org.ws4d.java.message.FaultMessage,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handle(Message request, FaultMessage fault, ProtocolData protocolData) {
		if (request.getType() != DPWSMessageConstants.GET_METADATA_MESSAGE) {
			Log.warn("DefaultDeviceReferenceCallback.handle(FaultMessage): unexpected fault message " + fault + ", request was " + request);
			return;
		}

		RequestSynchronizer sync = null;
		boolean doResend = false;
		try {
			synchronized (servRef) {
				sync = (RequestSynchronizer) servRef.synchronizers.get(request.getMessageId());
				if (sync == null) {
					Log.warn("No synchronizer found for request message " + request);
					return;
				}
				
				getTargetAddress().mergeProtocolInfo(protocolData.getProtocolInfo());

				Log.error("Get metadata request leads to fault message: " + fault);

				doResend = sync.hostedBlockVersion == servRef.hostedBlockVersion;
				if (!doResend) {
					servRef.synchronizers.remove(request.getMessageId());
				}
			}

			if (doResend) {
				XAddressInfo xAddressInfo = servRef.getNextXAddressInfoAfterFailure(request.getTargetAddress());
				request.setTargetXAddressInfo(xAddressInfo);
				OutDispatcher.getInstance().send((GetMetadataMessage) request, xAddressInfo, this);
			} else {
				sync.exception = new TimeoutException("Get metadata request leads to fault message: " + fault);
			}
		} catch (Throwable e) {
			sync.exception = new TimeoutException("Exception occured during fault processing: " + e);
		}

		synchronized (sync) {
			sync.pending = false;
			sync.notifyAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.ResponseCallback#handleMalformedResponseException
	 * (org.ws4d.java.message.Message, java.lang.Exception,
	 * org.ws4d.java.communication.ProtocolData)
	 */
	public void handleMalformedResponseException(Message request, Exception exception, ProtocolData protocolData) {
		if (causedByResolve(request)) {
			return;
		}
		if (request.getType() != DPWSMessageConstants.GET_METADATA_MESSAGE) {
			Log.warn("Unexpected malformed response, request was " + request);
			/*
			 * We could "return;" here, but for DPWS2006 GET is send for devices
			 * and services, so we MUST check it ....
			 */

		}

		RequestSynchronizer sync = null;
		boolean doResend = false;
		try {
			synchronized (servRef) {
				sync = (RequestSynchronizer) servRef.synchronizers.get(request.getMessageId());
				if (sync == null) {
					Log.warn("No synchronizer found for request message " + request);
					return;
				}
				
				Log.error("Get metadata request leads to an exception: " + exception);

				doResend = sync.hostedBlockVersion == servRef.hostedBlockVersion;
				if (!doResend) {
					servRef.synchronizers.remove(request.getMessageId());
				}
			}

			if (doResend) {
				XAddressInfo xAddressInfo = servRef.getNextXAddressInfoAfterFailure(request.getTargetAddress());
				request.setTargetXAddressInfo(xAddressInfo);
				OutDispatcher.getInstance().send((GetMetadataMessage) request, xAddressInfo, this);
			} else {
				sync.exception = new TimeoutException("Get metadata request leads to an exception: " + exception);
			}
		} catch (Throwable e) {
			if (sync instanceof GetMetadataRequestSynchronizer) {
				GetMetadataRequestSynchronizer gmsync = (GetMetadataRequestSynchronizer) sync;
				Service service = servRef.createProxyServiceFromLocalMetadata();
				if (service != null) {
					gmsync.service = service;
				} else {
					sync.exception = new TimeoutException("Exception occured during malformed response processing: " + e);
				}
			} else {
				sync.exception = new TimeoutException("Exception occured during malformed response processing: " + e);
			}
		}

		synchronized (sync) {
			sync.pending = false;
			sync.notifyAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ws4d.java.communication.DefaultResponseCallback#
	 * handleTransmissionException(org.ws4d.java.message.Message,
	 * java.lang.Exception, org.ws4d.java.communication.ProtocolData)
	 */
	public void handleTransmissionException(Message request, Exception exception, ProtocolData protocolData) {
		if (causedByResolve(request)) {
			return;
		}
		if (request.getType() != DPWSMessageConstants.GET_METADATA_MESSAGE) {
			Log.warn("Unexpected transmission exception, request was " + request);
			return;
		}

		RequestSynchronizer sync = null;
		boolean doResend = false;
		try {
			synchronized (servRef) {
				sync = (RequestSynchronizer) servRef.synchronizers.get(request.getMessageId());
				if (sync == null) {
					Log.warn("No synchronizer found for request message " + request);
					return;
				}
				
				Log.error("Get metadata request leads to transmission exception: " + exception);

				doResend = sync.hostedBlockVersion == servRef.hostedBlockVersion;
				if (!doResend) {
					servRef.synchronizers.remove(request.getMessageId());
				}
			}

			if (doResend) {
				XAddressInfo xAddressInfo = servRef.getNextXAddressInfoAfterFailure(request.getTargetAddress());
				request.setTargetXAddressInfo(xAddressInfo);
				OutDispatcher.getInstance().send((GetMetadataMessage) request, xAddressInfo, this);
			}  else {
				sync.exception = new TimeoutException("Get metadata request leads to transmission exception: " + exception);
			}
		} catch (Throwable e) {
			sync.exception = new TimeoutException("Exception occured during transmission exception processing: " + e);
		}

		synchronized (sync) {
			sync.pending = false;
			sync.notifyAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.ws4d.java.communication.DefaultResponseCallback#handleTimeout(org
	 * .ws4d.java.message.Message)
	 */
	public void handleTimeout(Message request) {
		if (causedByResolve(request)) {
			return;
		}
		if (request.getType() != DPWSMessageConstants.GET_METADATA_MESSAGE) {
			Log.warn("Unexpected timeout, request was " + request);
			return;
		}

		RequestSynchronizer sync = null;
		boolean doResend = false;
		try {
			synchronized (servRef) {
				sync = (RequestSynchronizer) servRef.synchronizers.get(request.getMessageId());
				if (sync == null) {
					Log.warn("No synchronizer found for request message " + request);
					return;
				}
				Log.error("Get metadata request timeout.");

				doResend = sync.hostedBlockVersion == servRef.hostedBlockVersion;
				if (!doResend) {
					servRef.synchronizers.remove(request.getMessageId());
				}
			}

			if (doResend) {
				XAddressInfo xAddressInfo = servRef.getNextXAddressInfoAfterFailure(request.getTargetAddress());
				request.setTargetXAddressInfo(xAddressInfo);
				OutDispatcher.getInstance().send((GetMetadataMessage) request, xAddressInfo, this);
			} else {
				sync.exception = new TimeoutException("Get metadata request timeout.");
			}
		} catch (Throwable e) {
			sync.exception = new TimeoutException("Exception occured during timeout processing: " + e);
		}

		synchronized (sync) {
			sync.pending = false;
			sync.notifyAll();
		}
	}

	private boolean causedByResolve(Message request) {
		if (request.getType() == DPWSMessageConstants.RESOLVE_MESSAGE) {
			ResolveRequestSynchronizer sync;
			synchronized (servRef) {
				sync = (ResolveRequestSynchronizer) servRef.synchronizers.get(request.getMessageId());
				if (sync == null) {
					/*
					 * this usually occurs when a resolve request times out
					 * after a valid resolve matches has been received; we may
					 * ignore this silently
					 */
					/*
					 * this shouldn't ever happen, as it would mean we receive a
					 * response to a request we never sent...
					 */
					// Log.warn("DefaultDeviceReferenceCallback: ignoring unexpected ResolveMatches message "
					// + request);
					return true;
				}
				if (sync.hostedBlockVersion == servRef.hostedBlockVersion) {
					if (maybeSendNextResolve(sync, request.getProtocolInfo())) {
						return true;
					}
				}
				if (sync == servRef.resolveSynchronizer) {
					servRef.resolveSynchronizer = null;
				}
			}
			synchronized (sync) {
				sync.pending = false;
				sync.notifyAll();
			}
			return true;
		}
		return false;
	}

}