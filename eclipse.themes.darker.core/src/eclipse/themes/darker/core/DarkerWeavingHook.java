package eclipse.themes.darker.core;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.osgi.service.component.ComponentContext;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Jin Mingjian(jin.phd@gmail.com)
 * 
 */
public class DarkerWeavingHook implements WeavingHook {

  private static String WEAVINGHOOK_STORE = "weavinghookstore";

  private static int    weavingEnabled    = 0;

  protected void activate(ComponentContext context) {
    File hookingStore = context.getBundleContext().getDataFile(
        WEAVINGHOOK_STORE);
    FileReader reader = null;
    try {
      if (!hookingStore.exists())
        hookingStore.createNewFile();
      
      reader = new FileReader(hookingStore);
      weavingEnabled = reader.read();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      close(reader);
    }
  }

  protected void deactivate(ComponentContext context) {
    File hookingStore = context.getBundleContext().getDataFile(
        WEAVINGHOOK_STORE);
    FileWriter writer = null;
    try {
      writer = new FileWriter(hookingStore);
      writer.write(weavingEnabled);
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      close(writer);
    }
  }

  private static void close(Closeable c) {
    try {
      if (c != null)
        c.close();
    } catch (IOException e) {
      // TODO
    }
  }

  @Override
  public void weave(WovenClass wovenClass) {
    if (weavingEnabled==0) return;
    
    try {
      String className = wovenClass.getClassName();
      if (className.equals("org.eclipse.swt.custom.CLabel")) {
        transform(wovenClass);
      }
    } catch (ClassCircularityError cce) {
      // TODO
    }
  }

  public void transform(WovenClass wovenClass) {
    byte[] bytes = wovenClass.getBytes();

    ClassReader cr = new ClassReader(bytes);
    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS
        | ClassWriter.COMPUTE_FRAMES);
    cr.accept(cw, 0);

    Method md = Method
        .getMethod("org.eclipse.swt.graphics.Color getForeground ()");
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, md.getName(),
        md.getDescriptor(), null, null);
    
    final GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, md, mv);
    final Label LABEL_SKIP_CALL_TO_SUPER = new Label();
    final Label LABEL_LOOP_CMP = new Label();
    final Label LABEL_LOOP_START = new Label();
    
    mg.newInstance(Type.getType(Exception.class));
    mg.dup();
    mg.invokeConstructor(Type.getType(Exception.class), 
        Method.getMethod("void <init> ()"));
    mg.invokeVirtual(Type.getType(Exception.class), 
        Method.getMethod("StackTraceElement[] getStackTrace ()"));
    mg.storeLocal(1,Type.getType(StackTraceElement[].class));

    mg.push(10);
    mg.storeLocal(2,Type.getType(int.class));
    
    mg.goTo(LABEL_LOOP_CMP);

    mg.mark(LABEL_LOOP_START);
    mg.loadLocal(1);
    mg.loadLocal(2);
    mg.arrayLoad(Type.getType(StackTraceElement.class));
    mg.invokeVirtual(Type.getType(StackTraceElement.class), 
        Method.getMethod("String getClassName ()"));
    mg.push("Dialog");
    mg.invokeVirtual(Type.getType(String.class), 
        Method.getMethod("int indexOf (String)"));
    mg.push(-1);
    mg.ifICmp(GeneratorAdapter.EQ, LABEL_SKIP_CALL_TO_SUPER);
//    //XXX: DEBUG
//    mg.getStatic(Type.getType(System.class), "out", Type.getType(java.io.PrintStream.class));
//    mg.push("2");
//    mg.invokeVirtual(Type.getType(java.io.PrintStream.class), Method.getMethod("void println (String)"));
//    //XXX: DEBUG
    mg.loadThis();
    mg.invokeConstructor(Type.getType("Lorg/eclipse/swt/widgets/Control;"), 
        Method.getMethod("org.eclipse.swt.graphics.Color getForeground ()"));
    mg.returnValue();
    
    mg.mark(LABEL_SKIP_CALL_TO_SUPER);
    mg.iinc(2, 1);
    
    mg.mark(LABEL_LOOP_CMP);
    mg.loadLocal(2);
    mg.loadLocal(1);
    mg.arrayLength();
    mg.ifICmp(GeneratorAdapter.LT, LABEL_LOOP_START);
    
    mg.loadThis();
    mg.invokeVirtual(Type.getType("Lorg/eclipse/swt/custom/CLabel;"),
        Method.getMethod("org.eclipse.swt.widgets.Display getDisplay ()"));
    mg.push(1);
    mg.invokeVirtual(Type.getType("Lorg/eclipse/swt/widgets/Display;"),
        Method.getMethod("org.eclipse.swt.graphics.Color getSystemColor (int)"));
    mg.returnValue();
    
    mg.endMethod();
    cw.visitEnd();

    wovenClass.setBytes(cw.toByteArray());
  }

  public static boolean hasWeavingEnabled() {
    return weavingEnabled == 1 ? true : false;
  }

  public static void enableWeaving() {
    weavingEnabled = 1;
  }

  public static void disableWeaving() {
    weavingEnabled = 0;
  }
}
