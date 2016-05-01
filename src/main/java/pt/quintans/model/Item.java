package pt.quintans.model;

public class Item {
    protected String model;
    protected String domain;
    protected String type;
    protected String instance;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String instanciate(String arg) {
        if(instance != null) {
            return String.format(instance, arg);
        } else {
            return "";
        }
    }
    
    @Override
    public String toString() {
        return domain;
    }
}
