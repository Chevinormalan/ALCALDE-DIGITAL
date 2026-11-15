package alcaldedigital.compartido.guardado;
/** Mismo LCG de java.util.Random, con estado explícito para continuar la secuencia al recuperar. */
public final class AzarPartida extends java.util.Random {
 private long estado;
 public AzarPartida(long semilla){super(0);setSeed(semilla);}
 @Override public synchronized void setSeed(long semilla){estado=(semilla^0x5DEECE66DL)&((1L<<48)-1);}
 @Override protected synchronized int next(int bits){estado=(estado*0x5DEECE66DL+11)&((1L<<48)-1);return (int)(estado>>>(48-bits));}
 public long estado(){return estado;}
 public void restaurar(long v){if(v<0||v>=(1L<<48))throw new IllegalArgumentException("Azar inválido");estado=v;}
}
