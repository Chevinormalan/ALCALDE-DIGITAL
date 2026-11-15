package alcaldedigital.pruebas;
import org.junit.jupiter.api.extension.*;import java.nio.file.*;
/** Nunca se leen ni escriben las partidas del usuario al ejecutar JUnit. */
public final class PerfilAislado implements TestInstancePreConstructCallback,BeforeEachCallback,AfterEachCallback {
 private String anterior;private boolean preparado;
 private void aislar()throws Exception{anterior=System.getProperty("ad.guardados");System.setProperty("ad.guardados",Files.createTempDirectory("alcalde-junit-").toString());preparado=true;}
 public void preConstructTestInstance(TestInstanceFactoryContext f,ExtensionContext c)throws Exception{aislar();}
 public void beforeEach(ExtensionContext c)throws Exception{if(!preparado)aislar();}
 public void afterEach(ExtensionContext c){if(anterior==null)System.clearProperty("ad.guardados");else System.setProperty("ad.guardados",anterior);preparado=false;}
}
