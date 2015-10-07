package org.artifactory.api.governance;

/**
 * @author Chen Keinan
 */
public class BaseAction {

    private String name;

    BaseAction() {
    }
    public BaseAction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
