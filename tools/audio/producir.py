#!/usr/bin/env python3
"""Partituras originales, stems separados y master PCM. Python/numpy + FluidSynth + FFmpeg.
Los WAV de vidIQ y el SF2 se suministran por --fuentes; no se necesitan al jugar.
"""
import argparse, json, math, struct, subprocess, wave, hashlib
from pathlib import Path
import numpy as np
ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'src/main/resources/cliente/audio'
CAT=[
 ('G01','Ciudad Nova despierta',104,0,'menu'),('G02','Escuchar antes de publicar',82,2,'dialogo'),('G03','Lo que queda de la ciudad',88,0,'final'),
 ('M101','Una tarde en Ciudad Nova',92,5,'parque'),('M102','El rumor corre',124,9,'urgencia'),('M201','Turno de redacción',100,2,'oficina'),('M202','Seis equipos, una red',132,9,'urgencia'),
 ('M301','NovaMall después del cierre',110,4,'misterio'),('M302','Publicaciones con cuerpo',144,4,'combate'),('M303','Cortar la cadena',152,9,'jefe'),
 ('M401','Camino a la plaza',112,7,'viaje'),('M402','Defender la palabra',144,7,'combate'),('M501','Dentro de la conexión',96,2,'misterio'),('M502','Dentro del servidor',150,2,'jefe'),
 ('M601','Por encima de Ciudad Nova',124,5,'vuelo'),('M602','Romper la interferencia',148,5,'jefe'),('M701','Última hora',104,0,'noticias'),('M702','La primicia no espera',160,9,'carrera'),('M801','La plaza decide',100,0,'ceremonia'),('M802','El futuro de Civitas',156,9,'jefe'),('G03-reconstruccion','Lo que queda · reconstrucción',88,5,'reconstruccion'),('G03-crisis','Lo que queda · ciudad en crisis',76,0,'crisis')]
SR=22050; PPQ=480

def vlq(n):
 b=[n&127];n>>=7
 while n:b.insert(0,128|(n&127));n>>=7
 return bytes(b)

def midi(path,bpm,parts,bars=32):
 tracks=[]
 tempo=int(60000000/bpm)
 tracks.append(b'\0\xff\x51\x03'+tempo.to_bytes(3,'big')+vlq(bars*4*PPQ)+b'\xff\x2f\0')
 for ch,program,notes,pan in parts:
  events=[(0,bytes([0xc0+ch,program])),(0,bytes([0xb0+ch,10,pan])),(0,bytes([0xb0+ch,91,28]))]
  for beat,dur,pitch,vel in notes:
   t=round(beat*PPQ);end=round((beat+dur)*PPQ)
   events.extend([(t,bytes([0x90+ch,pitch,vel])),(end,bytes([0x80+ch,pitch,0]))])
  events.sort(key=lambda e:(e[0],e[1][0]&0xf0!=0x80))
  data=b'';last=0
  for t,msg in events:data+=vlq(t-last)+msg;last=t
  data+=vlq(max(0,bars*4*PPQ-last))+b'\xff\x2f\0';tracks.append(data)
 path.write_bytes(b'MThd'+struct.pack('>IHHH',6,1,len(tracks),PPQ)+b''.join(b'MTrk'+struct.pack('>I',len(t))+t for t in tracks))

def score(id,bpm,key,kind):
 """32 bars A A' B A'', composed by harmonic functions and original melodic phrases."""
 action=kind in ('combate','jefe','urgencia','carrera'); minor=action or kind in ('misterio','crisis')
 roots=([0,5,3,7,0,8,5,7] if minor else [0,7,9,5,0,4,5,7])
 offset=sum(map(ord,id))%4
 if id=='M802':roots=[0,8,5,7,3,8,5,7]
 elif id in ('M502','M402'):roots=[0,3,8,7,0,5,8,7]
 elif id=='M602':roots=[0,5,8,7,3,8,5,7]
 motif=([0,3,7,10,7,5,3,2] if minor else [0,4,7,11,9,7,4,2])
 pad=[];lead=[];bass=[];arp=[];drums=[];epic=[]
 for bar in range(32):
  root=48+key+roots[(bar//2)%8];beat=bar*4
  quality=3 if minor or (roots[(bar//2)%8] in (9,4)) else 4
  chord=[root,root+quality,root+7,root+10 if minor else root+11]
  for p in chord[:3]:pad.append((beat,3.85,p,40 if action else 46))
  # Eight distinct phrases; B moves register/rhythm, final cadence resolves the motif.
  phrase=(bar//4)%8;register=12+(12 if 16<=bar<24 else 0)
  rhythm=[0,.5,1.5,2,3] if action else [0,1,2.5,3.25]
  for i,t in enumerate(rhythm):
   p=60+key+motif[(bar*2+i+offset+(2 if phrase%2 else 0))%8]+(12 if 16<=bar<24 else 0)
   if bar%4==3 and i==len(rhythm)-1:p=60+key+(7 if bar<31 else 0)
   lead.append((beat+t,.38 if action else .62,p,68+(bar%4)*3))
  for i in range(8 if action else 4):
   step=.5 if action else 1
   arp.append((beat+i*step,.23 if action else .43,chord[(i+bar)%4]+12,42+(i%2)*7))
  for i in range(4 if action else 2):bass.append((beat+i*(1 if action else 2),.65 if action else 1.5,root-12+(7 if i%4==3 else 0),74))
  for t in ([0,1.5,2,3.5] if action else [0,2]):drums.append((beat+t,.12,36,85))
  for t in [1,3]:drums.append((beat+t,.12,38 if action else 37,66))
  for i in range(8):drums.append((beat+i*.5,.08,42,42+(i%2)*12))
  if bar%8==7:
   for i in range(4):drums.append((beat+3+i*.25,.1,45+i%3,64+i*4))
  for p in [root+12,root+19]:epic.append((beat,1.4,p,68))
  for t in [0,2]:epic.append((beat+t, .32,root,80))
 # Instrument palettes per context, not randomized shared melodies.
 instruments={'crisis':(0,48,43,10),'reconstruccion':(0,48,32,8),'urgencia':(11,45,38,80),'jefe':(61,48,38,81),'combate':(60,48,34,80),'misterio':(10,89,39,98),'vuelo':(81,48,34,46),'carrera':(80,45,38,81),'ceremonia':(8,48,32,46),'noticias':(11,45,33,80),'viaje':(56,45,33,24)}
 lp,pp,bp,ap=instruments.get(kind,(8,48,33,46))
 return [(0,pp,pad,48),(1,lp,lead,70),(2,bp,bass,64),(3,ap,arp,84)],[(9,0,drums,64)],[(4,60,epic,58)]

def readwav(path):
 with wave.open(str(path)) as w:
  assert w.getframerate()==SR and w.getsampwidth()==2
  x=np.frombuffer(w.readframes(w.getnframes()),'<i2').astype(np.float32).reshape(-1,w.getnchannels())/32768
  return np.repeat(x,2,axis=1) if x.shape[1]==1 else x

def writewav(path,x):
 path.parent.mkdir(parents=True,exist_ok=True)
 with wave.open(str(path),'wb') as w:w.setnchannels(2);w.setsampwidth(2);w.setframerate(SR);w.writeframes((np.clip(x,-.98,.98)*32767).astype('<i2').tobytes())

def render(sf,mid,target):
 subprocess.run(['fluidsynth','-ni','-q','-g','0.55','-R','0.25','-C','0.15','-r',str(SR),'-F',str(target),str(sf),str(mid)],check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)

def main():
 arg=argparse.ArgumentParser();arg.add_argument('--fuentes',type=Path,required=True);arg.add_argument('--solo',nargs='*');a=arg.parse_args();work=a.fuentes;sf=work/'GeneralUser-GS.sf2';manifest=[]
 for id,title,bpm,key,kind in CAT:
  if a.solo and id not in a.solo:continue
  duration=32*4*60/bpm;n=round(duration*SR);generated=(work/(id+'.wav')).exists();layers=[]
  if generated:
   tmp=work/(id+'-pcm.wav')
   subprocess.run(['ffmpeg','-v','error','-y','-i',str(work/(id+'.wav')),'-ar',str(SR),'-ac','2',str(tmp)],check=True)
   x=readwav(tmp);# crossfade final 1.25 seconds into beginning, retaining opening and musical continuity
   k=min(round(SR*1.25),len(x)//8);fade=np.linspace(0,1,k)[:,None]
   x[-k:]=x[-k:]*(1-fade)+x[:k]*fade;x=np.concatenate((x[-k:],x[k:-k]))
   x*=min(1.7,.74/(np.max(np.abs(x))+1e-9));writewav(OUT/'musica'/f'{id}.wav',x)
   duration=len(x)/SR;source='vidIQ · composición generada'
  else:
   parts=score(id,bpm,key,kind);arrays=[]
   for layer,part in zip(['base','ritmo','epica'],parts):
    mid=ROOT/'docs/audio/fuentes'/f'{id}-{layer}.mid';midi(mid,bpm,part)
    wav=work/f'{id}-{layer}.wav';render(sf,mid,wav);x=readwav(wav)
    # Fold the release/reverb into start: exact 32-bar cyclic stems with shared frame count.
    if len(x)>n:
     tail=x[n:];m=min(len(tail),n);x[:m]+=tail[:m]
    x=x[:n];arrays.append(x)
   peak=max(np.max(np.abs(arrays[0]+arrays[1]+arrays[2])),.01);gain=.75/peak
   for layer,x in zip(['','-ritmo','-epica'],arrays):writewav(OUT/'musica'/f'{id}{layer}.wav',x*gain)
   layers=['ritmo','epica'];source='Partitura original MIDI · GeneralUser GS / FluidSynth'
  manifest.append(dict(id=id,titulo=title,bpm=bpm,contexto=kind,segundos=round(duration,3),capas=layers,origen=source,sha256=hashlib.sha256((OUT/'musica'/f'{id}.wav').read_bytes()).hexdigest()))
  print(id,round(duration,1),source,flush=True)
 # Event sounds original deterministic synthesis: short envelopes, distinct pitch/shape identities.
 rng=np.random.default_rng(8008)
 names=['hover','click','volver','bloqueado','confirmar','pausa','reanudar','guardar','dialogo','civitas','verificar','reportar','compartir','rectificar','rumor','alarma','reparar','salto','aterrizar','esquiva','golpe','escudo','rayo_aviso','rayo','orbe','espadas','pinchos','teletransporte','fase','absorcion','gravedad','turno','cuenta','salida','recoger','pacto','urna','escrutinio','inyeccion','mutacion','recuperacion','victoria','derrota','disparo_final','explosion_final','parque','oficina','red','plaza','viento']
 for i,name in enumerate(names):
  ambience=name in ['parque','oficina','red','plaza','viento'];dur=4 if ambience else (1.5 if name in ['victoria','derrota','explosion_final','mutacion'] else .065 if name=='hover' else .045 if name=='dialogo' else .28)
  t=np.arange(round(SR*dur))/SR
  if ambience:
   noise=rng.normal(0,1,len(t));x=np.convolve(noise,np.ones(100)/100,mode='same')*.09
   if name in ['red','oficina']:x+=.018*np.sin(2*np.pi*60*t)+.008*np.sin(2*np.pi*180*t)
   else:x+=.018*np.sin(2*np.pi*330*t)*np.sin(2*np.pi*t*.5)**12
   k=800;fade=np.linspace(0,1,k);x[:k]*=fade;x[-k:]*=fade[::-1]
  else:
   f={'hover':880,'click':660,'volver':440,'bloqueado':180,'dialogo':500,'alarma':740,'golpe':110,'inyeccion':1200,'mutacion':85,'victoria':523,'derrota':220}.get(name,220+(i%8)*65)
   sign=-1 if name in ['derrota','golpe','bloqueado','mutacion'] else 1
   phase=2*np.pi*f*(t+sign*.24*t*t/dur)
   env=np.minimum(1,t/.007)*np.exp(-t/(dur*.24))*np.minimum(1,(dur-t)/.015)
   x=(np.sin(phase)+.25*np.sin(phase*2))*.23*env
   if name in ['espadas','rayo','golpe','mutacion','explosion_final','pinchos']:x+=rng.normal(0,.12,len(t))*env
   if name in ['victoria','recuperacion','confirmar','guardar']:x+=np.sin(phase*1.5)*.09*env
  writewav(OUT/'fx'/f'{name}.wav',np.stack([x,x],axis=1))
 if a.solo and (OUT/'catalogo.json').exists():
  prev=json.loads((OUT/'catalogo.json').read_text())['pistas'];manifest=[x for x in prev if x['id'] not in a.solo]+manifest
 (OUT/'catalogo.json').write_text(json.dumps({'version':1,'frecuencia':SR,'pistas':manifest,'efectos':names},ensure_ascii=False,indent=2)+'\n')
 # Machine-readable Java manifest kept deliberately small.
 props=[]
 for item in manifest:
  props.extend([f'{item["id"]}.titulo={item["titulo"]}',f'{item["id"]}.capas={",".join(item["capas"])}'])
 fx=[str(x.relative_to(OUT.parent))[:-4] for x in OUT.parent.rglob('*.wav') if 'audio/musica' not in str(x) and 'loop' not in x.name]
 props.append('precarga='+','.join(fx))
 (OUT/'catalogo.properties').write_text('\n'.join(props)+'\n')
if __name__=='__main__':main()
