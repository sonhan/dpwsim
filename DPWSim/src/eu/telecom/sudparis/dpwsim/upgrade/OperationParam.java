package eu.telecom.sudparis.dpwsim.upgrade;

import java.io.Serializable;

public class OperationParam implements Serializable {
    private static final long serialVersionUID = 6076127035197513908L;
    private String request;
    private String response;
    private String url;

    public OperationParam(String request, String response, String url){
        this.request = request;
        this.response = response;
        this.url = url;
    }

    public String getRequest() {
        return request;
    }
    public void setRequest(String request) {
        this.request = request;
    }
    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String toString(){
        return request;
    }
}
