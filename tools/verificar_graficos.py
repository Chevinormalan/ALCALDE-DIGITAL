#!/usr/bin/env python3
"""Ventana, pantalla completa, clics y ocho misiones; todos los perfiles son temporales."""
from pathlib import Path
import subprocess,tempfile,json,sys,os
base=Path(__file__).resolve().parents[1]
evid=Path(os.environ.get('AD_GRAFICOS_EVIDENCIAS',base/'docs/graficos/evidencias')).resolve();evid.mkdir(parents=True,exist_ok=True)
cp=':'.join([os.environ.get('AD_GRAFICOS_JAR',str(base/'target/classes')),str(base/'target/test-classes'),str(Path.home()/'.m2/repository/org/processing/core/4.5.6/core-4.5.6.jar'),str(Path.home()/'.m2/repository/net/java/jinput/jinput/2.0.10/jinput-2.0.10.jar')])
res=[]
for caso in (sys.argv[1:] or ['panel']+[f'mision{i}' for i in range(1,9)]):
 with tempfile.TemporaryDirectory(prefix='alcalde-graficos-') as perfil:
  args=['java','--enable-native-access=ALL-UNNAMED',f'-Dad.guardados={perfil}','-Dad.audio.mudo=true',f'-Dad.graficos.caso={caso}',f'-Dad.graficos.evidencias={evid}','-Dnet.java.games.input.librarypath='+str(base/'distribucion/nativos'),'-cp',cp,'alcaldedigital.integracion.RevisionGraficos']
  r=subprocess.run(args,cwd=base,capture_output=True,text=True,timeout=45)
  (evid/f'{caso}.log').write_text(r.stdout+r.stderr)
  ok=r.returncode==0 and f'GRAFICOS_REVISION_OK {caso}' in r.stdout
  res.append({'caso':caso,'correcto':ok});print(caso,ok,flush=True)
  if not ok:print((r.stdout+r.stderr)[-4000:],flush=True);break
(evid/'resultados.json').write_text(json.dumps(res,indent=2)+'\n')
if not all(r['correcto'] for r in res):raise SystemExit(1)
