#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile,json,wave
import numpy as np
base=Path(__file__).resolve().parents[2]
evid=base/'docs/audio/evidencias';evid.mkdir(parents=True,exist_ok=True)
cp=':'.join([str(base/'target/classes'),str(base/'target/test-classes'),str(Path.home()/'.m2/repository/org/processing/core/4.5.6/core-4.5.6.jar'),str(Path.home()/'.m2/repository/net/java/jinput/jinput/2.0.10/jinput-2.0.10.jar')])
with tempfile.TemporaryDirectory(prefix='alcalde-audio-') as perfil:
 log=evid/'audio.log';log.write_text('')
 args=['java','--enable-native-access=ALL-UNNAMED',f'-Dad.guardados={perfil}',f'-Dad.audio.registro={log}',f'-Dad.audio.captura={evid}/mezcla-real.wav',f'-Dad.audio.evidencias={evid}','-Dnet.java.games.input.librarypath='+str(base/'distribucion/nativos'),'-cp',cp,'alcaldedigital.integracion.RevisionAudio']
 r=subprocess.run(args,cwd=base,capture_output=True,text=True,timeout=55)
 (evid/'ventana.log').write_text(r.stdout+r.stderr)
 if r.returncode or 'AUDIO_REVISION_OK' not in r.stdout:raise SystemExit((r.stdout+r.stderr)[-4000:])
 texto=log.read_text();assert 'DISPOSITIVO_OK' in texto,texto
 for pista in ['G01','M101','M201','M802']:assert f'MUSICA_OK {pista} ' in texto,(pista,texto)
 with wave.open(str(evid/'mezcla-real.wav')) as w:
  x=np.frombuffer(w.readframes(w.getnframes()),'<i2').astype(float)/32768
  info={'segundos':w.getnframes()/w.getframerate(),'pico':float(np.abs(x).max()),'rms':float(np.sqrt(np.mean(x*x))),'clipping':int((np.abs(x)>.999).sum())}
 assert info['rms']>.005 and info['clipping']==0,info
 (evid/'resultado.json').write_text(json.dumps({'correcto':True,'perfil_temporal':True,'audio_real':info},indent=2)+'\n');print('AUDIO_NATIVO_OK',info)
