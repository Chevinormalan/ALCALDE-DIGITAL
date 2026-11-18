import subprocess,pathlib,tempfile,json
base=pathlib.Path(__file__).resolve().parents[1]
evid=base/'docs/integracion-misiones46';evid.mkdir(parents=True,exist_ok=True)
saves=tempfile.mkdtemp(prefix='alcalde-integracion46-grafica-')
# Victories are fixtures in this temporary profile; the real campaign is never unlocked here.
(pathlib.Path(saves)/'progreso-misiones.properties').write_text('version=1\n'+''.join(f'victoria.{n}=true\n' for n in range(1,8)))
casos=[(4,'CAR',False),(4,'PLAZA',False),(4,'BATTLE',False),(4,'BATTLE',True),(4,'RUMOR',False),(4,'COMPLETE',False),(6,'DISCURSO',False),(6,'COMBATE',False),(6,'ENTREVISTA',False),(6,'COMBATE',True),(6,'MODO',False),(6,'FINAL',False)]
res=[]
for n,fase,pausa in casos:
 nombre=f'mision{n}-{fase.lower()}'+('-pausa' if pausa else '')
 args=['java','--enable-native-access=ALL-UNNAMED','-Dad.mision46.revision=true',f'-Dad.mision46.numero={n}',f'-Dad.mision46.fase={fase}',f'-Dad.mision46.pausa={str(pausa).lower()}',f'-Dad.mision46.captura={evid}/{nombre}.png','-Dad.mision46.captura.frame=105','-Dad.mision4.sinGuardar=true','-Dad.mision6.sinGuardar=true',f'-Dad.guardados={saves}','-Dad.audio.mudo=true','-Dnet.java.games.input.librarypath='+str(base/'distribucion/nativos'),'-cp','distribucion/alcalde-digital.jar:distribucion/lib/*','alcaldedigital.app.ClienteApp']
 with (evid/(nombre+'.log')).open('w') as out:
  try: p=subprocess.run(args,cwd=base,stdout=out,stderr=subprocess.STDOUT,timeout=30);code=p.returncode
  except subprocess.TimeoutExpired:code=124
 log=(evid/(nombre+'.log')).read_text();ok=code==0 and f'MISION_{n}_INTEGRACION_OK' in log and (evid/(nombre+'.png')).exists()
 res.append(dict(escena=nombre,codigo=code,ok=ok));print(nombre,ok,flush=True)
 if not ok: print(log[-2500:],flush=True);break
(evid/'resultados.json').write_text(json.dumps(res,ensure_ascii=False,indent=2))
if not all(x['ok'] for x in res):raise SystemExit(1)
