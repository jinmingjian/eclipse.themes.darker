package eclipse.themes.darker.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.List;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.osgi.framework.wiring.BundleWiring;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class DarkerWeavingHookTest {
  
  @Test
  public void simpleWeaving() {
    DarkerWeavingHook dwh = new DarkerWeavingHook();
    WovenClassImpl wovenClass = new WovenClassImpl(org.eclipse.swt.custom.CLabel.class);

    System.out.println(wovenClass.bytes.length);
    dwh.weave(wovenClass);
    System.out.println(wovenClass.bytes.length);
    
    wovenClass.verifyGetForegroundMethod();
  }
  
  
  static class WovenClassImpl extends ClassLoader implements WovenClass  {
    
    byte[] bytes = null;
    
    public WovenClassImpl(Class clazz) {
      try {
        ClassReader cr = new ClassReader(clazz.getName());
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cr.accept(cw, 0);
        bytes = cw.toByteArray();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    @Override
    public byte[] getBytes() {
      return bytes;
    }

    @Override
    public void setBytes(byte[] newBytes) {
      bytes = newBytes;
    }

    @Override
    public List<String> getDynamicImports() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean isWeavingComplete() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public String getClassName() {
      return "org.eclipse.swt.custom.CLabel";
    }

    @Override
    public ProtectionDomain getProtectionDomain() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Class<?> getDefinedClass() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public BundleWiring getBundleWiring() {
      // TODO Auto-generated method stub
      return null;
    }
    
    //XXX for debug
    public void dumpTo(String fileName) {
      
    }
    
    public void pipe(InputStream is, OutputStream os) throws IOException {
      int n;
      byte[] buffer = new byte[4096];
      while((n = is.read(buffer)) > -1) {
        os.write(buffer, 0, n);   // Don't allow any extra bytes to creep in, final write
      }
     os.close ();
    }
    
    public void verifyGetForegroundMethod() {
      boolean hasGetForeground = false;
      
      Class<?> newCLabelClass = this.defineClass(null, bytes, 0, bytes.length);
      Method[] methods = newCLabelClass.getDeclaredMethods();
      for (int i = 0; i < methods.length; i++) {
        Method m = methods[i];
        if (m.getName().equals("getForeground")) {
          hasGetForeground = true;
        }
      }
      
      assertThat(hasGetForeground,is(true));
    }
    
  }

}
