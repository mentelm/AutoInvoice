package pl.mentelm.autoinvoice.templates;

import java.util.Locale;
import java.util.Map;

public interface Template {

    String getTemplateName();

    Map<String, Object> getContextMap(Locale locale);
}
