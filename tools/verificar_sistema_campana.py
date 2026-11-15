"""Revisión gráfica del selector, pausa común, checkpoints y recuperación, con perfiles temporales."""
import subprocess,pathlib,tempfile,json
base=pathlib.Path(__file__).resolve().parents[1]
evid=base/'docs/campana/evidencias';evid.mkdir(parents=True,exist_ok=True)
res=[]
def lanzar(n,perfil,nombre,continuar=False,ayuda=False):
 args=['java','--enable-native-access=ALL-UNNAMED','-Dad.sistema.revision=true',f'-Dad.sistema.mision={n}',f'-Dad.sistema.continuar={str(continuar).lower()}',f'-Dad.sistema.ayuda={str(ayuda).lower()}',f'-Dad.sistema.captura={evid}/{nombre}.png',f'-Dad.guardados={perfil}','-Dad.audio.mudo=true','-Dnet.java.games.input.librarypath='+str(base/'distribucion/nativos'),'-cp','distribucion/alcalde-digital.jar:distribucion/lib/*','alcaldedigital.app.ClienteApp']
 with (evid/(nombre+'.log')).open('w') as out:
  try:p=subprocess.run(args,cwd=base,stdout=out,stderr=subprocess.STDOUT,timeout=45);codigo=p.returncode
  except subprocess.TimeoutExpired:codigo=124
 log=(evid/(nombre+'.log')).read_text();ok=codigo==0 and 'SISTEMA_CAMPANA_OK' in log and (evid/(nombre+'.png')).exists()
 res.append(dict(escena=nombre,codigo=codigo,ok=ok));print(nombre,ok,flush=True)
 if not ok:print(log[-3000:],flush=True);raise RuntimeError(nombre)
try:
 perfil=pathlib.Path(tempfile.mkdtemp(prefix='alcalde-campana-selector-'))
 lanzar(0,perfil,'selector-bloqueado')
 for n in range(1,8):
  perfil=pathlib.Path(tempfile.mkdtemp(prefix=f'alcalde-campana-{n}-'))
  # Fixture explícita: simula haber ganado las misiones anteriores únicamente en este perfil temporal.
  (perfil/'progreso-misiones.properties').write_text('version=1\n'+''.join(f'victoria.{i}=true\n' for i in range(1,n)))
  lanzar(n,perfil,f'mision{n}-pausa')
  if n in (1,2,3,4,6):
   checkpoint=perfil/f'mision{n}.properties'
   if not checkpoint.exists():raise RuntimeError(f'No se guardó la misión {n}')
   lanzar(n,perfil,f'mision{n}-recuperada',True)
 lanzar(5,perfil,'mision5-ayuda',ayuda=True)
finally:
 (evid/'resultados.json').write_text(json.dumps(res,ensure_ascii=False,indent=2))
if not res or not all(x['ok'] for x in res):raise SystemExit(1)
