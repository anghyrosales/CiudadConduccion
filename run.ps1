param (
    [Parameter(Position = 0)]
    [ValidateSet("1", "2", "3", "4", "test", "package")]
    [string]$Opcion = "4"
)

Set-Location $PSScriptRoot

switch ($Opcion) {
    "1" {
        Write-Host "Iniciando Clase 1..." -ForegroundColor Cyan
        mvn compile exec:exec -Pclase1
    }
    "2" {
        Write-Host "Iniciando Clase 2..." -ForegroundColor Cyan
        mvn compile exec:exec -Pclase2
    }
    "3" {
        Write-Host "Iniciando Clase 3..." -ForegroundColor Cyan
        mvn compile exec:exec -Pclase3
    }
    "4" {
        Write-Host "Iniciando Clase 4 (Juego Completo)..." -ForegroundColor Cyan
        mvn compile exec:exec -Pclase4
    }
    "test" {
        Write-Host "Ejecutando pruebas unitarias..." -ForegroundColor Green
        mvn test
    }
    "package" {
        Write-Host "Empaquetando proyecto..." -ForegroundColor Green
        mvn package
    }
}
