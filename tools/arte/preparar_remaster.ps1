param([string]$Raiz = (Split-Path (Split-Path $PSScriptRoot)))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies 'System.Drawing' -Path (Join-Path $PSScriptRoot 'ArteRemaster.cs')
$recursos = Join-Path $Raiz 'src/main/resources/cliente'
$referencia = Join-Path $PSScriptRoot 'referencias-remaster/sofi-recogida.png'
$m1 = Join-Path $recursos 'arte/mision1'
$m6 = Join-Path $recursos 'mision6'
[ArteRemaster]::Sofi($referencia,(Join-Path $m1 'sofi-recogida.png'))
[ArteRemaster]::Helicoptero((Join-Path $m6 'helicoptero.png'),(Join-Path $m6 'helicoptero-cuerpo.png'),(Join-Path $m6 'helicoptero-rotor.png'))
[ArteRemaster]::Explosion((Join-Path $m6 'explosion-remaster.png'))
[ArteRemaster]::Hallazgo((Join-Path $recursos 'audio/fx/lupa-hallazgo.wav'))
$poses = Join-Path $recursos 'presentacion/remaster'
[IO.Directory]::CreateDirectory($poses) | Out-Null
foreach ($actor in @(
    @{Nombre='periodista'; Pivote=233; Fila1=392; Fila2=393; Alto=352},
    @{Nombre='influencer'; Pivote=232; Fila1=409; Fila2=408; Alto=381},
    @{Nombre='juan'; Pivote=214; Fila1=414; Fila2=412; Alto=318},
    @{Nombre='sofi'; Pivote=229; Fila1=385; Fila2=388; Alto=350},
    @{Nombre='palomo'; Pivote=226; Fila1=372; Fila2=370; Alto=355; Alfa=222}
)) {
    $original = Join-Path $PSScriptRoot ('referencias-remaster/'+$actor.Nombre+'-movimiento.png')
    $alfa = if ($actor.ContainsKey('Alfa')) { $actor.Alfa } else { 140 }
    [ArteRemaster]::Poses($original,(Join-Path $poses ($actor.Nombre+'.png')),$actor.Pivote,$actor.Fila1,$actor.Fila2,$actor.Alto,$alfa)
}
[ArteRemaster]::Rivales((Join-Path $PSScriptRoot 'referencias-remaster/rivales-movimiento.png'),$poses)
Add-Type -Path (Join-Path $PSScriptRoot 'AudioRemaster.cs')
[AudioRemaster]::Producir((Join-Path $recursos 'audio/remaster'))
