
package de.beuth.bva.flagspot.model;

import java.util.HashMap;
import java.util.Map;

public class Translations {

    private String de;
    private String es;
    private String fr;
    private String it;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The de
     */
    public String getDe() {
        return de;
    }

    /**
     * @param de The de
     */
    public void setDe(String de) {
        this.de = de;
    }

    /**
     * @return The es
     */
    public String getEs() {
        return es;
    }

    /**
     * @param es The es
     */
    public void setEs(String es) {
        this.es = es;
    }

    /**
     * @return The fr
     */
    public String getFr() {
        return fr;
    }

    /**
     * @param fr The fr
     */
    public void setFr(String fr) {
        this.fr = fr;
    }

    /**
     * @return The it
     */
    public String getIt() {
        return it;
    }

    /**
     * @param it The it
     */
    public void setIt(String it) {
        this.it = it;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
