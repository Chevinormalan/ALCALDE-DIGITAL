#!/usr/bin/env python3
"""Verifica los recursos reales y exporta un montaje de escucha a volumen de referencia."""
from pathlib import Path
import json,wave,hashlib
import numpy as np
base=Path(__file__).resolve().parents[2];audio=base/'src/main/resources/cliente/audio';evid=base/'docs/audio/evidencias';evid.mkdir(parents=True,exist_ok=True)
cat=json.loads((audio/'catalogo.json').read_text());out=[];montaje=[]
def leer(path):
 with wave.open(str(path)) as w:
  assert (w.getframerate(),w.getsampwidth(),w.getnchannels())==(22050,2,2),path
  return np.frombuffer(w.readframes(w.getnframes()),'<i2').astype(np.float32).reshape(-1,2)/32768
for item in cat['pistas']:
 path=audio/'musica'/(item['id']+'.wav');x=leer(path);total=x.copy()
 assert hashlib.sha256(path.read_bytes()).hexdigest()==item['sha256'],path
 for layer in item['capas']:
  stem=leer(audio/'musica'/f'{item["id"]}-{layer}.wav');assert stem.shape==x.shape;total+=stem
 info={'id':item['id'],'segundos':round(len(x)/22050,3),'pico_capas_completas':round(float(np.abs(total).max()),5),'rms':round(float(np.sqrt(np.mean(total*total))),5),'salto_bucle':round(float(np.abs(total[0]-total[-1]).max()),5)}
 assert info['pico_capas_completas']<.99 and info['rms']>.003,info
 out.append(info)
 if item['id'] in ['G01','M102','M202','M303','M502','M602','M702','M802','G03-crisis']:
  seg=total[22050*4:22050*10].copy();ramp=np.linspace(0,1,2205)[:,None];seg[:2205]*=ramp;seg[-2205:]*=ramp[::-1];montaje.append(seg*.7)
fx=[]
for path in sorted((audio/'fx').glob('*.wav')):
 x=leer(path);fx.append({'id':path.stem,'pico':round(float(np.abs(x).max()),5),'segundos':len(x)/22050})
 assert np.abs(x).max()<.99,path
mix=np.concatenate(montaje)
with wave.open(str(evid/'muestra-banda-sonora.wav'),'wb') as w:w.setnchannels(2);w.setsampwidth(2);w.setframerate(22050);w.writeframes((mix*32767).astype('<i2').tobytes())
report={'pistas':len(out),'stems':sum(len(x['capas']) for x in cat['pistas']),'efectos':len(fx),'musica':out,'fx':fx}
(evid/'auditoria-recursos.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(json.dumps({'pistas':len(out),'stems':report['stems'],'efectos':len(fx),'segundos_musica':round(sum(x['segundos'] for x in out),1),'pico_maximo':max(x['pico_capas_completas'] for x in out),'max_salto_bucle':max(x['salto_bucle'] for x in out)}))
