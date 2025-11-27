from setuptools import setup, find_packages

with open("pythonmc/README.md", "r", encoding="utf-8") as fh:
    long_description = fh.read()

setup(
    name="pythonmc-mod",
    version="3.0.0-alpha",
    author="PythonMC Team",
    description="Python library for PythonMC Minecraft mod",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/tu-usuario/pythonmc",
    packages=find_packages(),
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Development Status :: 3 - Alpha",
        "Intended Audience :: Developers",
        "Topic :: Games/Entertainment",
    ],
    python_requires=">=3.6",
    install_requires=[
        # No dependencies - uses only stdlib
    ],
)
