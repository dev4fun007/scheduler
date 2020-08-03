package bytes.sync.entity;

import javax.persistence.Embeddable;

@Embeddable
public class ActivationExpression {

    private String url;
    private String method;
    private String payload;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Method: " + method + ", Url: " + url + ", Payload: " + payload;
    }
}
