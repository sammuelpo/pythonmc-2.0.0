# Guía de Distribución vía GitHub

Si no quieres usar PyPI, **GitHub es la mejor alternativa**. Es gratis, fácil y muy común para mods y librerías pequeñas.

## Ventajas
- No necesitas registrarte en PyPI
- No hay problemas de nombres "ya cogidos"
- Tus usuarios pueden instalar directamente desde tu código

## Paso 1: Subir tu código a GitHub

1. Crea un nuevo repositorio en GitHub (ej: `pythonmc-lib`)
2. Sube los archivos de la carpeta `pythonmc` (y `setup.py`)
3. Asegúrate de que `setup.py` esté en la raíz del repositorio

## Paso 2: Cómo instalan tus usuarios

En lugar de `pip install pythonmc`, tus usuarios usarán:

```bash
pip install git+https://github.com/TU-USUARIO/pythonmc-lib.git
```

¡Y listo! Pip descargará e instalará la librería directamente desde GitHub.

## Paso 3: Facilitarlo aún más (Opcional)

Puedes crear un archivo `install.bat` en tu mod para que los usuarios solo tengan que hacer doble clic:

```batch
@echo off
echo Instalando librería PythonMC...
pip install git+https://github.com/TU-USUARIO/pythonmc-lib.git
pause
```
