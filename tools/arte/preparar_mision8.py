#!/usr/bin/env python3
"""Inspección y extracción sin alterar las imágenes generadas para misión 8."""
from PIL import Image
from pathlib import Path
import hashlib,json
base=Path(__file__).resolve().parents[2]
recursos=base/'src/main/resources/cliente/mision8'
fuentes=base/'tools/arte/referencias-mision8'
acciones=['reposo','carrera_contacto','carrera_paso','despegue','ascenso','caida','aterrizaje','esquiva','ataque_preparacion','ataque_activo','ataque_recuperacion','alcance_preparacion','alcance_activo','proteccion','dano','derrota']
efectos=['constructores','nave','humo','palomo','orbes','bots','pinchos','plataformas']
candidatos=['juan','petrosqui','abel','alberto']
manifest={'herramienta':'imagegen integrada','referencia':'arte vigente de misiones 1, 2, 3 y 7','paleta':['#21152F','#38264B','#604077','#BD91D5','#F5E8FB','#F4CB6A'],'atlas':{},'cuadros':[]}
for nombre in ['heroes','candidatos','efectos','escenarios','tarima-cuatro','infeccion-juan','infeccion-petrosqui','infeccion-abel','infeccion-alberto','ataques-forma-1','ataques-forma-2','ataques-forma-3']:
 p=recursos/f'{nombre}.png';im=Image.open(p).convert('RGBA');cols=1 if nombre=='tarima-cuatro' else 2 if nombre=='escenarios' else 4 if nombre.startswith('infeccion-') else 8;rows=4 if nombre.startswith('ataques-forma-') else cols
 manifest['atlas'][nombre]={'tamano':im.size,'columnas':cols,'filas':rows,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()}
 if nombre not in ('escenarios','tarima-cuatro'):
  assert im.getextrema()[3][0]==0,'El atlas debe tener alfa real'
 for i in range(cols*rows):
  x=i%cols;y=i//cols;celda=im.crop((x*im.width//cols,y*im.height//rows,(x+1)*im.width//cols,(y+1)*im.height//rows))
  if nombre=='heroes':ruta=Path(nombre)/['sofi','periodista','influencer','juan'][i//16]/f'{i%16:02d}-{acciones[i%16]}.png'
  elif nombre=='candidatos':ruta=Path(nombre)/candidatos[i//16]/f'{i%16:02d}.png'
  elif nombre=='efectos':ruta=Path(nombre)/efectos[y]/f'{x:02d}.png'
  elif nombre.startswith('infeccion-'):ruta=Path(nombre)/f'{i:02d}.png'
  elif nombre.startswith('ataques-forma-'):ruta=Path(nombre)/candidatos[y]/f'{x:02d}.png'
  elif nombre=='tarima-cuatro':ruta=Path(nombre)/'fondo.png'
  else:ruta=Path(nombre)/['tarima','arena','plataformas','ascenso'][i]/'fondo.png'
  salida=fuentes/'cuadros'/ruta;salida.parent.mkdir(parents=True,exist_ok=True);celda.save(salida)
  assert celda.getbbox(),f'Celda vacía {nombre} {i}'
  manifest['cuadros'].append({'atlas':nombre,'indice':i,'archivo':str(ruta),'dimensiones':celda.size,'sha256':hashlib.sha256(celda.tobytes()).hexdigest()})
assert len({c['sha256'] for c in manifest['cuadros']})==357,'Hay cuadros idénticos'
(fuentes/'manifest.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False))
print('352 sprites distintos con alfa y 5 fondos (incluye la nueva tarima); extracción y hashes correctos.')
