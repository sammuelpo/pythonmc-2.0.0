package com.pythonmc.mod.nodes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import com.pythonmc.mod.animation.AnimationSystem;
import com.pythonmc.mod.core.ProjectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Nodo 3D que renderiza modelos con texturas y animaciones
 * Integra el Model Editor, Texture Editor y Animation System
 */
public class Model3DNode extends BaseNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(Model3DNode.class);
    
    // Modelo 3D
    private String modelFile = "model.json";
    private Model3D model;
    
    // Textura
    private String textureFile = "";
    private ResourceLocation textureResource;
    private DynamicTexture dynamicTexture;
    
    // Animación
    private AnimationSystem.AnimationPlayer animationPlayer;
    private String currentAnimation = "idle";
    
    // Transformación
    private float posX = 0f, posY = 0f, posZ = 0f;
    private float rotX = 0f, rotY = 0f, rotZ = 0f;
    private float scaleX = 1f, scaleY = 1f, scaleZ = 1f;
    
    // Visibilidad
    private boolean visible = true;
    private boolean wireframe = false;
    
    // Cache de texturas
    private static final Map<String, ResourceLocation> textureCache = new HashMap<>();
    
    public Model3DNode(Level world, String name) {
        super(world, name);
        this.nodeType = "Model3D";
        
        // Cargar modelo por defecto
        loadModel();
        
        // Crear animation player
        AnimationSystem animSystem = AnimationSystem.getInstance(world);
        animationPlayer = animSystem.createPlayer(name);
        
        // Iniciar animación idle
        playAnimation("idle");
    }
    
    @Override
    public void init() {
        LOGGER.info("Model3DNode inicializado: {}", name);
    }
    
    @Override
    public void update() {
        // Actualizar animación
        updateAnimation();
        
        // Actualizar transformación desde animación
        updateTransformFromAnimation();
    }
    
    @Override
    public void render(PoseStack poseStack, VertexConsumer consumer, float partialTick) {
        if (!visible || model == null) return;
        
        poseStack.pushPose();
        
        // Aplicar transformación
        poseStack.translate(posX, posY, posZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
        poseStack.scale(scaleX, scaleY, scaleZ);
        
        // Renderizar modelo
        renderModel(poseStack, consumer);
        
        poseStack.popPose();
    }
    
    private void renderModel(PoseStack poseStack, VertexConsumer consumer) {
        Matrix4f matrix = poseStack.last().pose();
        
        // Renderizar cubos del modelo
        for (Model3D.ModelCube cube : model.cubes) {
            renderCube(poseStack, consumer, cube);
        }
    }
    
    private void renderCube(PoseStack poseStack, VertexConsumer consumer, Model3D.ModelCube cube) {
        poseStack.pushPose();
        
        // Posicionar el cubo
        poseStack.translate(cube.x + cube.width/2f, cube.y + cube.height/2f, cube.z + cube.depth/2f);
        
        // Escalar el cubo
        poseStack.scale(cube.width, cube.height, cube.depth);
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Aplicar textura si está disponible
        if (textureResource != null) {
            RenderSystem.setShaderTexture(0, textureResource);
        }
        
        // Renderizar las 6 caras del cubo con UV mapping
        renderCubeFace(consumer, matrix, cube.color, 0); // Front
        renderCubeFace(consumer, matrix, cube.color, 1); // Back
        renderCubeFace(consumer, matrix, cube.color, 2); // Top
        renderCubeFace(consumer, matrix, cube.color, 3); // Bottom
        renderCubeFace(consumer, matrix, cube.color, 4); // Right
        renderCubeFace(consumer, matrix, cube.color, 5); // Left
        
        poseStack.popPose();
    }
    
    private void renderCubeFace(VertexConsumer consumer, Matrix4f matrix, int color, int face) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;
        
        float s = 0.5f;
        
        // UV coordinates for each face
        float[] uvs = getFaceUVs(face);
        
        switch (face) {
            case 0: // Front
                consumer.vertex(matrix, -s, -s, s).color(r, g, b, a).uv(uvs[0], uvs[1]).endVertex();
                consumer.vertex(matrix, s, -s, s).color(r, g, b, a).uv(uvs[2], uvs[3]).endVertex();
                consumer.vertex(matrix, s, s, s).color(r, g, b, a).uv(uvs[4], uvs[5]).endVertex();
                consumer.vertex(matrix, -s, s, s).color(r, g, b, a).uv(uvs[6], uvs[7]).endVertex();
                break;
            case 1: // Back
                consumer.vertex(matrix, -s, -s, -s).color(r, g, b, a).uv(uvs[0], uvs[1]).endVertex();
                consumer.vertex(matrix, -s, s, -s).color(r, g, b, a).uv(uvs[2], uvs[3]).endVertex();
                consumer.vertex(matrix, s, s, -s).color(r, g, b, a).uv(uvs[4], uvs[5]).endVertex();
                consumer.vertex(matrix, s, -s, -s).color(r, g, b, a).uv(uvs[6], uvs[7]).endVertex();
                break;
            case 2: // Top
                consumer.vertex(matrix, -s, s, -s).color(r, g, b, a).uv(uvs[0], uvs[1]).endVertex();
                consumer.vertex(matrix, -s, s, s).color(r, g, b, a).uv(uvs[2], uvs[3]).endVertex();
                consumer.vertex(matrix, s, s, s).color(r, g, b, a).uv(uvs[4], uvs[5]).endVertex();
                consumer.vertex(matrix, s, s, -s).color(r, g, b, a).uv(uvs[6], uvs[7]).endVertex();
                break;
            case 3: // Bottom
                consumer.vertex(matrix, -s, -s, -s).color(r, g, b, a).uv(uvs[0], uvs[1]).endVertex();
                consumer.vertex(matrix, s, -s, -s).color(r, g, b, a).uv(uvs[2], uvs[3]).endVertex();
                consumer.vertex(matrix, s, -s, s).color(r, g, b, a).uv(uvs[4], uvs[5]).endVertex();
                consumer.vertex(matrix, -s, -s, s).color(r, g, b, a).uv(uvs[6], uvs[7]).endVertex();
                break;
            case 4: // Right
                consumer.vertex(matrix, s, -s, -s).color(r, g, b, a).uv(uvs[0], uvs[1]).endVertex();
                consumer.vertex(matrix, s, s, -s).color(r, g, b, a).uv(uvs[2], uvs[3]).endVertex();
                consumer.vertex(matrix, s, s, s).color(r, g, b, a).uv(uvs[4], uvs[5]).endVertex();
                consumer.vertex(matrix, s, -s, s).color(r, g, b, a).uv(uvs[6], uvs[7]).endVertex();
                break;
            case 5: // Left
                consumer.vertex(matrix, -s, -s, -s).color(r, g, b, a).uv(uvs[0], uvs[1]).endVertex();
                consumer.vertex(matrix, -s, -s, s).color(r, g, b, a).uv(uvs[2], uvs[3]).endVertex();
                consumer.vertex(matrix, -s, s, s).color(r, g, b, a).uv(uvs[4], uvs[5]).endVertex();
                consumer.vertex(matrix, -s, s, -s).color(r, g, b, a).uv(uvs[6], uvs[7]).endVertex();
                break;
        }
    }
    
    private float[] getFaceUVs(int face) {
        // UV coordinates for each face (0,0 to 1,1)
        return new float[]{0, 0, 1, 0, 1, 1, 0, 1};
    }
    
    private void updateAnimation() {
        if (animationPlayer != null) {
            AnimationSystem animSystem = AnimationSystem.getInstance(world);
            
            // Obtener valores de animación
            posX = animationPlayer.getValue("position.x", posX);
            posY = animationPlayer.getValue("position.y", posY);
            posZ = animationPlayer.getValue("position.z", posZ);
            
            rotX = animationPlayer.getValue("rotation.x", rotX);
            rotY = animationPlayer.getValue("rotation.y", rotY);
            rotZ = animationPlayer.getValue("rotation.z", rotZ);
            
            scaleX = animationPlayer.getValue("scale.x", scaleX);
            scaleY = animationPlayer.getValue("scale.y", scaleY);
            scaleZ = animationPlayer.getValue("scale.z", scaleZ);
        }
    }
    
    private void updateTransformFromAnimation() {
        // Este método actualiza la transformación basada en los valores de animación
        // La actualización real ocurre en updateAnimation()
    }
    
    private void loadModel() {
        try {
            File modelsDir = ProjectManager.getAssetsModelsFolder();
            File modelFile = new File(modelsDir, modelFile);
            
            if (modelFile.exists()) {
                model = Model3D.loadFromFile(modelFile);
                LOGGER.info("Modelo cargado: {}", modelFile.getName());
            } else {
                // Crear modelo por defecto
                model = new Model3D();
                model.addCube(new Model3D.ModelCube(0, 0, 0, 1, 1, 1));
                LOGGER.info("Usando modelo por defecto");
            }
        } catch (Exception e) {
            LOGGER.error("Error al cargar modelo", e);
            model = new Model3D();
        }
    }
    
    private void loadTexture() {
        if (textureFile.isEmpty()) {
            textureResource = null;
            return;
        }
        
        // Verificar cache
        if (textureCache.containsKey(textureFile)) {
            textureResource = textureCache.get(textureFile);
            return;
        }
        
        try {
            File texturesDir = ProjectManager.getAssetsTexturesFolder();
            File textureFile = new File(texturesDir, this.textureFile);
            
            if (textureFile.exists()) {
                BufferedImage image = ImageIO.read(textureFile);
                
                // Convertir a DynamicTexture
                com.mojang.blaze3d.platform.NativeImage nativeImage = new com.mojang.blaze3d.platform.NativeImage(image.getWidth(), image.getHeight(), false);
                
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        int rgb = image.getRGB(x, y);
                        nativeImage.setPixelRGBA(x, y, rgb);
                    }
                }
                
                dynamicTexture = new DynamicTexture(nativeImage);
                textureResource = Minecraft.getInstance().getTextureManager().register("pythonmc_" + name + "_texture", dynamicTexture);
                
                // Añadir a cache
                textureCache.put(this.textureFile, textureResource);
                
                LOGGER.info("Textura cargada: {}", textureFile.getName());
            } else {
                LOGGER.warn("Archivo de textura no encontrado: {}", textureFile);
                textureResource = null;
            }
        } catch (IOException e) {
            LOGGER.error("Error al cargar textura", e);
            textureResource = null;
        }
    }
    
    // ========== MÉTODOS PÚBLICOS ==========
    
    public void setModelFile(String modelFile) {
        this.modelFile = modelFile;
        loadModel();
    }
    
    public void setTextureFile(String textureFile) {
        this.textureFile = textureFile;
        loadTexture();
    }
    
    public void playAnimation(String animationName) {
        this.currentAnimation = animationName;
        AnimationSystem animSystem = AnimationSystem.getInstance(world);
        animSystem.play(animationName);
    }
    
    public void setPosition(float x, float y, float z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }
    
    public void setRotation(float x, float y, float z) {
        this.rotX = x;
        this.rotY = y;
        this.rotZ = z;
    }
    
    public void setScale(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public void setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
    }
    
    // ========== GETTERS ==========
    
    public String getModelFile() {
        return modelFile;
    }
    
    public String getTextureFile() {
        return textureFile;
    }
    
    public String getCurrentAnimation() {
        return currentAnimation;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public boolean isWireframe() {
        return wireframe;
    }
    
    public Model3D getModel() {
        return model;
    }
    
    // ========== CLASE INTERNA ==========
    
    /**
     * Representa un modelo 3D cargado desde archivo
     */
    public static class Model3D {
        public final java.util.List<ModelCube> cubes = new java.util.ArrayList<>();
        
        public void addCube(ModelCube cube) {
            cubes.add(cube);
        }
        
        public static Model3D loadFromFile(File file) throws IOException {
            // TODO: Implementar carga desde JSON
            Model3D model = new Model3D();
            
            // Por ahora, crear un cubo por defecto
            model.addCube(new ModelCube(0, 0, 0, 1, 1, 1));
            
            return model;
        }
        
        public static class ModelCube {
            public float x, y, z;
            public float width, height, depth;
            public int color = 0xFF6A9955;
            
            public ModelCube(float x, float y, float z, float width, float height, float depth) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.width = width;
                this.height = height;
                this.depth = depth;
            }
        }
    }
}
