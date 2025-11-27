# Instalación de PythonMC Library

## 🎯 Opción 1: Automática (Recomendada)

El paquete `pythonmc` se incluye automáticamente con el mod. Solo necesitas:

1. Instalar el mod PythonMC en Minecraft
2. Usar `import pythonmc` en tus scripts

**¡No necesitas instalar nada más!**

## 📦 Opción 2: Instalación Manual con pip

Si quieres usar `pythonmc` fuera de Minecraft (para desarrollo):

```bash
# Desde el directorio del proyecto
pip install -e .
```

O desde PyPI (cuando esté publicado):
```bash
pip install pythonmc
```

## 🧪 Verificar Instalación

Ejecuta este script para verificar:

```python
import pythonmc
print(f"pythonmc version: {pythonmc.__version__}")
print("✅ pythonmc instalado correctamente!")
```

## 🚀 Uso Básico

```python
from pythonmc import Engine

# Obtener nodo
camera = Engine.get_node("MainCamera")
camera.move(100, 70, 200)

# Crear nodo
audio = Engine.create_node("AudioPlayer", "Music")
audio.play()
```

## 📚 Documentación Completa

Ver `pythonmc/README.md` para la API completa.
