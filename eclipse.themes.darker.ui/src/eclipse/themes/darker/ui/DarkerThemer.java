package eclipse.themes.darker.ui;

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.prefs.BackingStoreException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eclipse.themes.darker.core.DarkerWeavingHook;
import static eclipse.themes.darker.ui.ThemeConstants.*;

@SuppressWarnings("restriction")
public class DarkerThemer {

  /**
   * The darker theme ID
   * NOTE: it keeps eclipse.themes.darker.theme rather than 
   *       eclipse.themes.darker.ui.theme for backward compatibility 
   */
  public static final String   THEME_DARKER_ID   = "eclipse.themes.darker.theme"; //$NON-NLS-1$
  
  private static final String THEME_DARKER_PREF_THEMEENABLED = "eclipse.themes.darker.theme_enabled"; //$NON-NLS-1$

  public static final String[] PREF_UI_KEYS      = {
      "AbstractTextEditor.Color.SelectionForeground.SystemDefault",
      "AbstractTextEditor.Color.SelectionBackground.SystemDefault",
      "AbstractTextEditor.Color.Background.SystemDefault",
      "AbstractTextEditor.Color.Foreground.SystemDefault",
      "AbstractTextEditor.Color.Background",
      "AbstractTextEditor.Color.FindScope",
      "AbstractTextEditor.Color.Foreground",
      "AbstractTextEditor.Color.SelectionBackground",
      "AbstractTextEditor.Color.SelectionForeground", "currentLineColor",
      "deletionIndicationColor", "javaScriptOccurrenceIndicationColor",
      "lineNumberColor", "occurrenceIndicationColor",
      "PHPReadOccurrenceIndicationColor", "printMarginColor" };

  public static final String[] PREF_JDT_KEYS     = { "java_bracket",
      "java_comment_task_tag", "java_default", "java_doc_default",
      "java_doc_keyword", "java_doc_link", "java_doc_tag", "java_keyword",
      "java_keyword_return", "java_multi_line_comment", "java_operator",
      "java_single_line_comment", "java_string", "pf_coloring_argument",
      "pf_coloring_assignment", "pf_coloring_comment", "pf_coloring_key",
      "pf_coloring_value",
      "semanticHighlighting.abstractMethodInvocation.color",
      "semanticHighlighting.abstractMethodInvocation.enabled",
      "semanticHighlighting.class.color", "semanticHighlighting.class.enabled",
      "semanticHighlighting.deprecatedMember.color",
      "semanticHighlighting.deprecatedMember.enabled",
      "semanticHighlighting.enum.color", "semanticHighlighting.enum.enabled",
      "semanticHighlighting.field.color", "semanticHighlighting.field.enabled",
      "semanticHighlighting.inheritedMethodInvocation.color",
      "semanticHighlighting.inheritedMethodInvocation.enabled",
      "semanticHighlighting.interface.color",
      "semanticHighlighting.interface.enabled",
      "semanticHighlighting.localVariable.color",
      "semanticHighlighting.localVariable.enabled",
      "semanticHighlighting.localVariableDeclaration.color",
      "semanticHighlighting.localVariableDeclaration.enabled",
      "semanticHighlighting.method.color",
      "semanticHighlighting.method.enabled",
      "semanticHighlighting.methodDeclarationName.color",
      "semanticHighlighting.methodDeclarationName.enabled",
      "semanticHighlighting.number.color",
      "semanticHighlighting.number.enabled",
      "semanticHighlighting.parameterVariable.color",
      "semanticHighlighting.parameterVariable.enabled",
      "semanticHighlighting.staticField.color",
      "semanticHighlighting.staticField.enabled",
      "semanticHighlighting.staticFinalField.color",
      "semanticHighlighting.staticFinalField.enabled",
      "semanticHighlighting.staticMethodInvocation.color",
      "semanticHighlighting.staticMethodInvocation.enabled",
      "semanticHighlighting.typeArgument.color",
      "semanticHighlighting.typeArgument.enabled",
      "semanticHighlighting.typeParameter.color",
      "semanticHighlighting.typeParameter.enabled" };

  @Inject
  IEventBroker                 eventBroker;

  private IEclipsePreferences  prefDarker, prefJDT, prefUI, prefPDE;;

  private boolean              isLastThemeDarker = false;

  @Execute
  public void onExecute() {
    eventBroker.subscribe(IThemeEngine.Events.THEME_CHANGED,
        new EventHandler() {
          public void handleEvent(Event event) {
            ITheme currentTheme = (ITheme) event
                .getProperty(IThemeEngine.Events.THEME);
            // if (!prefDarker.getBoolean(
            // THEME_DARKER_PREF_THEMEENABLED, false))
            if (currentTheme.getId().equals(THEME_DARKER_ID)) {
              setupPreferences();
              isLastThemeDarker = true;
              DarkerWeavingHook.enableWeaving();
              hookDarkerCore();
            } else if (isLastThemeDarker) {
              DarkerWeavingHook.disableWeaving();
              setToDefaultPreferences();
            }

          }
        });
  }

  @Inject
  private void setPrefReferences(
      @Preference(nodePath = Activator.PLUGIN_ID) IEclipsePreferences prefDarker,
      @Preference(nodePath = "org.eclipse.ui.editors") IEclipsePreferences prefUI,
      @Preference(nodePath = "org.eclipse.jdt.ui") IEclipsePreferences prefJDT,
      @Preference(nodePath = "org.eclipse.pde.ui") IEclipsePreferences prefPDE) {
    this.prefUI = prefUI;
    this.prefJDT = prefJDT;
    this.prefDarker = prefDarker;
    this.prefPDE = prefPDE;
  }

  private void setupPreferences() {
    // NOTE: for org.eclipse.ui.editors
    prefUI.putBoolean(
        "AbstractTextEditor.Color.SelectionForeground.SystemDefault", false);
    prefUI.putBoolean(
        "AbstractTextEditor.Color.SelectionBackground.SystemDefault", false);
    prefUI.putBoolean("AbstractTextEditor.Color.Background.SystemDefault",
        false);
    prefUI.putBoolean("AbstractTextEditor.Color.Foreground.SystemDefault",
        false);
    prefUI.put("AbstractTextEditor.Color.Background", "25,25,25");
    prefUI.put("AbstractTextEditor.Color.FindScope", "25,25,25");
    prefUI.put("AbstractTextEditor.Color.Foreground", "255,255,255");
    prefUI.put("AbstractTextEditor.Color.SelectionBackground", "65,76,59");
    prefUI.put("AbstractTextEditor.Color.SelectionForeground", "255,255,255");
    prefUI.put("currentLineColor", "34,34,32");
    prefUI.put("deletionIndicationColor", "255,0,0");
    // p.put("javaScriptOccurrenceIndicationColor","97,97,97");
    prefUI.put("lineNumberColor", "102,102,102");
    prefUI.put("occurrenceIndicationColor", "97,97,97");
    // p.put("PHPReadOccurrenceIndicationColor","97,97,97");
    prefUI.put("printMarginColor", "102,102,102");

    // NOTE: for org.eclipse.jdt.ui
    prefJDT.put("java_bracket", "255,255,255");
    prefJDT.put("java_comment_task_tag", "128,0,128");
    prefJDT.put("java_default", "255,255,255");
    prefJDT.put("java_doc_default", "140,63,200");
    prefJDT.put("java_doc_keyword", "128,0,128");
    prefJDT.put("java_doc_link", "129,69,130");
    prefJDT.put("java_doc_tag", "128,0,128");
    prefJDT.put("java_keyword", "236,105,30");
    prefJDT.put("java_keyword_return", "236,105,30");
    prefJDT.put("java_multi_line_comment", "140,63,200");
    prefJDT.put("java_operator", "255,255,255");
    prefJDT.put("java_single_line_comment", "129,70,162");
    prefJDT.put("java_string", "71,116,136");

    prefJDT.put("pf_coloring_argument", "236,105,30");
    prefJDT.put("pf_coloring_assignment", "255,255,255");
    prefJDT.put("pf_coloring_comment", "129,70,162");
    prefJDT.put("pf_coloring_key", "255,255,255");
    prefJDT.put("pf_coloring_value", "71,116,136");

    prefJDT.put("semanticHighlighting.abstractMethodInvocation.color",
        "241,196,54");
    prefJDT.putBoolean("semanticHighlighting.abstractMethodInvocation.enabled",
        true);
    prefJDT.put("semanticHighlighting.class.color", "156,248,40");
    prefJDT.putBoolean("semanticHighlighting.class.enabled", true);
    prefJDT.put("semanticHighlighting.deprecatedMember.color", "255,255,255");
    prefJDT.putBoolean("semanticHighlighting.deprecatedMember.enabled", true);
    prefJDT.put("semanticHighlighting.enum.color", "64,128,0");
    prefJDT.putBoolean("semanticHighlighting.enum.enabled", true);
    prefJDT.put("semanticHighlighting.field.color", "53,122,143");
    prefJDT.putBoolean("semanticHighlighting.field.enabled", true);
    prefJDT.put("semanticHighlighting.inheritedMethodInvocation.color",
        "227,183,53");
    prefJDT.putBoolean(
        "semanticHighlighting.inheritedMethodInvocation.enabled", true);
    prefJDT.put("semanticHighlighting.interface.color", "135,240,37");
    prefJDT.putBoolean("semanticHighlighting.interface.enabled", true);
    prefJDT.put("semanticHighlighting.localVariable.color", "60,117,141");
    prefJDT.putBoolean("semanticHighlighting.localVariable.enabled", true);
    prefJDT.put("semanticHighlighting.localVariableDeclaration.color",
        "53,122,146");
    prefJDT.putBoolean("semanticHighlighting.localVariableDeclaration.enabled",
        true);
    prefJDT.put("semanticHighlighting.method.color", "247,197,39");
    prefJDT.putBoolean("semanticHighlighting.method.enabled", true);
    prefJDT.put("semanticHighlighting.methodDeclarationName.color",
        "247,197,39");
    prefJDT.putBoolean("semanticHighlighting.methodDeclarationName.enabled",
        true);
    prefJDT.put("semanticHighlighting.number.color", "71,116,136");
    prefJDT.putBoolean("semanticHighlighting.number.enabled", true);
    prefJDT.put("semanticHighlighting.parameterVariable.color", "64,128,0");
    prefJDT.putBoolean("semanticHighlighting.parameterVariable.enabled", true);
    prefJDT.put("semanticHighlighting.staticField.color", "255,255,255");
    prefJDT.putBoolean("semanticHighlighting.staticField.enabled", true);
    prefJDT.put("semanticHighlighting.staticFinalField.color", "128,255,0");
    prefJDT.putBoolean("semanticHighlighting.staticFinalField.enabled", true);
    prefJDT.put("semanticHighlighting.staticMethodInvocation.color",
        "255,255,255");
    prefJDT.putBoolean("semanticHighlighting.staticMethodInvocation.enabled",
        true);
    prefJDT.put("semanticHighlighting.typeArgument.color", "217,176,172");
    prefJDT.putBoolean("semanticHighlighting.typeArgument.enabled", true);
    prefJDT.put("semanticHighlighting.typeParameter.color", "205,177,173");
    prefJDT.putBoolean("semanticHighlighting.typeParameter.enabled", true);

    prefPDE.put(ThemeConstants.PDE_P_XML_COMMENT,
        ThemeConstants.PDE_XML_COMMENT);
    prefPDE.put(ThemeConstants.PDE_P_PROC_INSTR, ThemeConstants.PDE_PROC_INSTR);
    prefPDE.put(ThemeConstants.PDE_P_STRING, ThemeConstants.PDE_STRING);
    prefPDE.put(ThemeConstants.PDE_P_EXTERNALIZED_STRING,
        ThemeConstants.PDE_EXTERNALIZED_STRING);
    prefPDE.put(ThemeConstants.PDE_P_DEFAULT, ThemeConstants.PDE_DEFAULT);
    prefPDE.put(ThemeConstants.PDE_P_TAG, ThemeConstants.PDE_TAG);
    prefPDE.put(ThemeConstants.PDE_P_HEADER_KEY, ThemeConstants.PDE_HEADER_KEY);
    prefPDE.put(ThemeConstants.PDE_P_HEADER_VALUE,
        ThemeConstants.PDE_HEADER_VALUE);
    prefPDE.put(ThemeConstants.PDE_P_HEADER_ASSIGNMENT,
        ThemeConstants.PDE_HEADER_ASSIGNMENT);
    prefPDE.put(ThemeConstants.PDE_P_HEADER_OSGI,
        ThemeConstants.PDE_HEADER_OSGI);
    prefPDE.put(ThemeConstants.PDE_P_HEADER_ATTRIBUTES,
        ThemeConstants.PDE_HEADER_ATTRIBUTES);

    // prefDarker.putBoolean(
    // THEME_DARKER_PREF_THEMEENABLED, true);

    try {
      prefUI.sync();
      prefJDT.sync();
      prefDarker.flush();
    } catch (BackingStoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @SuppressWarnings("deprecation")
  private void setToDefaultPreferences() {
    for (String key : PREF_UI_KEYS)
      ((AbstractUIPlugin) Platform.getPlugin("org.eclipse.ui.editors"))
          .getPreferenceStore().setToDefault(key);

    for (String key : PREF_JDT_KEYS)
      ((AbstractUIPlugin) Platform.getPlugin("org.eclipse.jdt.ui"))
          .getPreferenceStore().setToDefault(key);

    for (String key : PREF_PDE_KEYS)
      ((AbstractUIPlugin) Platform.getPlugin("org.eclipse.jdt.ui"))
          .getPreferenceStore().setToDefault(key);

    // try {
    // prefDarker.putBoolean(
    // THEME_DARKER_PREF_THEMEENABLED, false);
    // prefDarker.sync();
    // } catch (BackingStoreException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

  }

  private void hookDarkerCore() {
    Bundle bundle = FrameworkUtil.getBundle(DarkerWeavingHook.class);
    try {
      if (bundle.getState()==Bundle.RESOLVED)
        bundle.start();
    } catch (BundleException e) {
      // TODO use log in future
      e.printStackTrace();
    }
    bundle.adapt(BundleStartLevel.class).setStartLevel(1);
  }

}
