from pathlib import Path
import tempfile, subprocess, json
base=Path(__file__).resolve().parents[1]
evid=base/'docs/mision8/evidencias'
with tempfile.TemporaryDirectory(prefix='alcalde-m8-integracion-') as carpeta:
 d=Path(carpeta)
 (d/'progreso-misiones.properties').write_text('version=1\n'+''.join(f'victoria.{i}=true\n' for i in range(1,8)))
 p='version=1\n'
 senales=[{'publicaciones_responsables':3,'burlas':0},{'equipos_seguros':6,'fallo_critico':0},{'confianza_final':70,'desinformacion_final':20},{'respuestas_favorables':3,'respuestas_desfavorables':0,'respuestas_neutrales':1},{'publicaciones_verificadas':4,'rumores':0},{'respuestas_escucha':2,'respuestas_conviccion':1},{'juan_corrupto':0,'noticias_contrastadas':4}]
 for m,s in enumerate(senales,1):
  pre=f'mision.{m}.campana.'
  p+=pre+f'partida=fixture-{m}\n'+pre+'victoria=true\n'+pre+'puntaje=85\n'+pre+'datos.cantidad=0\n'+pre+'decisiones.cantidad=0\n'
  p+=''.join(pre+f'ciudad.{i}={3 if i==1 else 0}\n' for i in range(7))
  p+=pre+f'senales.cantidad={len(s)}\n'
  for i,(k,v) in enumerate(s.items()):p+=pre+f'senal.{i}.clave={k}\n'+pre+f'senal.{i}.valor={v}\n'
 (d/'tabla-puntajes.properties').write_text(p)
 args=['java','--enable-native-access=ALL-UNNAMED','-Dnet.java.games.input.librarypath='+str(base/'distribucion/nativos'),'-Dad.sistema.revision=true','-Dad.sistema.mision=8','-Dad.sistema.pausa=false',f'-Dad.sistema.captura={evid}/integracion.png',f'-Dad.guardados={carpeta}','-Dad.audio.mudo=true','-cp','distribucion/alcalde-digital.jar:distribucion/lib/*','alcaldedigital.app.ClienteApp']
 r=subprocess.run(args,cwd=base,capture_output=True,text=True,timeout=40)
 (evid/'integracion.log').write_text(r.stdout+r.stderr)
 ok=r.returncode==0 and 'SISTEMA_CAMPANA_OK: misión 8' in r.stdout
 print('Integración en ClienteSketch:',ok)
 if not ok:print(r.stdout+r.stderr);raise SystemExit(1)
