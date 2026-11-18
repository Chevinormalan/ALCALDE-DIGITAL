#!/usr/bin/env python3
"""Renderiza fixtures de misión 8 con el mismo renderer, usando perfiles temporales."""
from pathlib import Path
import subprocess,tempfile,json
base=Path(__file__).resolve().parents[1]
evid=base/'docs/mision8/evidencias';evid.mkdir(parents=True,exist_ok=True)
cp=':'.join([str(base/'target/classes'),str(base/'target/test-classes'),str(Path.home()/'.m2/repository/org/processing/core/4.5.6/core-4.5.6.jar'),str(Path.home()/'.m2/repository/net/java/jinput/jinput/2.0.10/jinput-2.0.10.jar'),str(Path.home()/'.m2/repository/org/junit/jupiter/junit-jupiter-api/5.11.4/junit-jupiter-api-5.11.4.jar'),str(Path.home()/'.m2/repository/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.jar'),str(Path.home()/'.m2/repository/org/junit/platform/junit-platform-commons/1.11.4/junit-platform-commons-1.11.4.jar')])
resultados=[]
import sys
animar="--animar" in sys.argv[1:];casos=[a for a in sys.argv[1:] if a!="--animar"]
for caso in (casos or ['config','obra','auditoria','voto','escrutinio','mutacion','fase1','fase2','fase3','fase4','fase5','derrota','final','otro-final','consulta']):
 with tempfile.TemporaryDirectory(prefix='alcalde-m8-revision-') as perfil:
  args=['java','-Dad.audio.mudo=true',f'-Dad.guardados={perfil}',f'-Dad.m8.caso={caso}',f'-Dad.m8.captura={evid}/{caso}.png','-cp',cp,'alcaldedigital.mision8.RevisionOcho']
  if animar:
   cuadros=Path(perfil)/'cuadros';cuadros.mkdir();frames=38 if caso.startswith('infeccion-') else 60
   args[1:1]=[f'-Dad.m8.secuencia={cuadros}',f'-Dad.m8.frames={frames}','-Dad.m8.tick=0','-Dad.m8.paso=4']
  r=subprocess.run(args,cwd=base,capture_output=True,text=True,timeout=90 if animar else 35)
  if animar and r.returncode==0:
   from PIL import Image
   capturas=[];paleta=None
   for archivo in sorted(cuadros.glob('*.png')):
    with Image.open(archivo) as im:
     rgb=im.convert('RGB')
     if paleta is None:paleta=rgb.quantize(colors=256,dither=Image.Dither.NONE)
     capturas.append(rgb.quantize(palette=paleta,dither=Image.Dither.NONE))
   assert len(capturas)==frames
   capturas[0].save(evid/f'{caso}.gif',save_all=True,append_images=capturas[1:],duration=67,loop=0,optimize=False)
   import shutil
   shutil.copyfile(cuadros/f'{min(frames,19):03d}.png',evid/f'{caso}.png')
  (evid/f'{caso}.log').write_text(r.stdout+r.stderr)
  ok=r.returncode==0 and f'MISION8_RENDER_OK {caso}' in r.stdout
  resultados.append({'caso':caso,'correcto':ok});print(caso,ok,flush=True)
  if not ok:print(r.stdout+r.stderr,flush=True)
(evid/('resultados-animacion.json' if animar else 'resultados.json')).write_text(json.dumps(resultados,indent=2))
if not all(r['correcto'] for r in resultados):raise SystemExit(1)
