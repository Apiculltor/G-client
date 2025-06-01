@echo off
:: ===================================================================
:: PRÉ-BUILD CHECK - VERIFICAÇÕES ANTES DO DEPLOY REMOTO
:: ===================================================================

echo.
echo ========================================
echo   PRÉ-BUILD CHECK - VUZIX BLADE
echo ========================================
echo.

set ERRORS=0

:: Verificar Java/JDK
echo 🔍 Verificando Java JDK...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java JDK não encontrado!
    set /a ERRORS+=1
) else (
    echo ✅ Java JDK OK
)

:: Verificar Gradle
echo 🔍 Verificando Gradle...
if exist "gradlew.bat" (
    echo ✅ Gradle Wrapper encontrado
) else (
    echo ❌ Gradle Wrapper não encontrado!
    set /a ERRORS+=1
)

:: Verificar Android SDK
echo 🔍 Verificando Android SDK...
adb version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Android SDK/ADB não encontrado!
    set /a ERRORS+=1
) else (
    echo ✅ Android SDK OK
)

:: Verificar estrutura do projeto
echo 🔍 Verificando estrutura do projeto...
if exist "app\build.gradle" (
    echo ✅ Módulo app encontrado
) else (
    echo ❌ Módulo app não encontrado!
    set /a ERRORS+=1
)

if exist "app\src\main\AndroidManifest.xml" (
    echo ✅ AndroidManifest.xml encontrado
) else (
    echo ❌ AndroidManifest.xml não encontrado!
    set /a ERRORS+=1
)

:: Verificar dependências Vuzix
echo 🔍 Verificando dependências Vuzix no build.gradle...
findstr /C:"vuzix" app\build.gradle >nul
if %errorlevel% equ 0 (
    echo ✅ Dependências Vuzix encontradas
) else (
    echo ⚠️  Dependências Vuzix não encontradas - verificar manualmente
)

:: Verificar permissões críticas
echo 🔍 Verificando permissões no AndroidManifest...
findstr /C:"CAMERA" app\src\main\AndroidManifest.xml >nul
if %errorlevel% equ 0 (
    echo ✅ Permissão CAMERA encontrada
) else (
    echo ❌ Permissão CAMERA não encontrada!
    set /a ERRORS+=1
)

findstr /C:"RECORD_AUDIO" app\src\main\AndroidManifest.xml >nul
if %errorlevel% equ 0 (
    echo ✅ Permissão RECORD_AUDIO encontrada
) else (
    echo ❌ Permissão RECORD_AUDIO não encontrada!
    set /a ERRORS+=1
)

:: Verificar espaço em disco
echo 🔍 Verificando espaço em disco...
for /f "tokens=3" %%a in ('dir /-c %SystemDrive%\ ^| find "bytes free"') do set FREE_SPACE=%%a
if %FREE_SPACE% gtr 1000000000 (
    echo ✅ Espaço em disco suficiente
) else (
    echo ⚠️  Pouco espaço em disco disponível
)

:: Verificar conexão de rede (para downloads de dependências)
echo 🔍 Verificando conexão de rede...
ping -n 1 google.com >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Conexão de rede OK
) else (
    echo ⚠️  Sem conexão de rede - builds podem falhar
)

echo.
echo ========================================
echo   RESUMO DA VERIFICAÇÃO
echo ========================================

if %ERRORS% equ 0 (
    echo ✅ TUDO OK! Projeto pronto para build e deploy
    echo.
    echo 🚀 Próximos passos:
    echo    1. Execute: scripts\build-release.bat
    echo    2. Execute: scripts\deploy-remote.bat
    echo.
) else (
    echo ❌ %ERRORS% erro(s) encontrado(s)!
    echo.
    echo 🔧 Corrija os problemas antes de continuar:
    echo    - Instale componentes faltantes
    echo    - Verifique configurações do projeto
    echo    - Configure variáveis de ambiente
    echo.
)

echo 📄 Documentação completa em: README.md
echo.
pause
