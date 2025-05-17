package io.scriptor.engine;

import io.scriptor.engine.data.MaterialInfo;
import io.scriptor.engine.data.MeshInfo;
import io.scriptor.engine.data.Vertex;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.assimp.*;

import java.io.InputStream;
import java.util.Optional;

import static org.lwjgl.assimp.Assimp.*;

public class ModelLoader {

    private ModelLoader() {
    }

    public static void loadModel(final @NotNull InputStream stream) {
        final var node = IYamlNode.load(stream);

        final var path = node
                .get("path")
                .as(String.class)
                .get();

        var flags = aiProcess_JoinIdenticalVertices;
        flags |= node
                .get("triangles")
                .as(Boolean.class)
                .mapBoolean(x -> x ? 0 : aiProcess_Triangulate)
                .or(aiProcess_Triangulate);
        flags |= node
                .get("textures")
                .as(Boolean.class)
                .mapBoolean(x -> x ? 0 : aiProcess_GenUVCoords)
                .or(aiProcess_GenUVCoords);
        flags |= node
                .get("normals")
                .as(Boolean.class)
                .mapBoolean(x -> x ? 0 : aiProcess_GenNormals)
                .or(aiProcess_GenNormals);
        flags |= node
                .get("flip")
                .as(Boolean.class)
                .mapBoolean(x -> x ? aiProcess_FlipWindingOrder : 0)
                .or(0);

        final var resources = new AIResourceIO();
        final var pScene    = aiImportFileEx(path, flags, resources.getFileIO());
        assert pScene != null;

        processMaterials(pScene);
        processMeshes(pScene);
    }

    private static void processMaterials(final @NotNull AIScene pScene) {
        final var ppMaterials = pScene.mMaterials();
        assert ppMaterials != null;
        for (int i = 0; i < pScene.mNumMaterials(); ++i) {
            final var pMaterial = AIMaterial.create(ppMaterials.get(i));

            final var pName             = AIString.create();
            final var pShadingMode      = new int[1];
            final var pIllumination     = new int[1];
            final var pAmbient          = AIColor4D.create();
            final var pDiffuse          = AIColor4D.create();
            final var pSpecular         = AIColor4D.create();
            final var pEmissive         = AIColor4D.create();
            final var pShininess        = new float[1];
            final var pOpacity          = new float[1];
            final var pTransparent      = AIColor4D.create();
            final var pAnisotropyFactor = new float[1];
            final var pRefractionIndex  = new float[1];

            aiGetMaterialString(pMaterial, "?mat.name", 0, 0, pName);
            aiGetMaterialIntegerArray(pMaterial, "$mat.shadingm", 0, 0, pShadingMode, null);
            aiGetMaterialIntegerArray(pMaterial, "$mat.illum", 0, 0, pIllumination, null);
            aiGetMaterialColor(pMaterial, "$clr.ambient", 0, 0, pAmbient);
            aiGetMaterialColor(pMaterial, "$clr.diffuse", 0, 0, pDiffuse);
            aiGetMaterialColor(pMaterial, "$clr.specular", 0, 0, pSpecular);
            aiGetMaterialColor(pMaterial, "$clr.emissive", 0, 0, pEmissive);
            aiGetMaterialFloatArray(pMaterial, "mat.shininess", 0, 0, pShininess, null);
            aiGetMaterialFloatArray(pMaterial, "mat.opacity", 0, 0, pOpacity, null);
            aiGetMaterialColor(pMaterial, "$clr.transparent", 0, 0, pTransparent);
            aiGetMaterialFloatArray(pMaterial, "$mat.anisotropyFactor", 0, 0, pAnisotropyFactor, null);
            aiGetMaterialFloatArray(pMaterial, "$mat.refractI", 0, 0, pRefractionIndex, null);

            final var info = new MaterialInfo(
                    pName.dataString(),
                    pShadingMode[0],
                    pIllumination[0],
                    new Vector4f(pAmbient.r(), pAmbient.g(), pAmbient.b(), pAmbient.a()),
                    new Vector4f(pDiffuse.r(), pDiffuse.g(), pDiffuse.b(), pDiffuse.a()),
                    new Vector4f(pSpecular.r(), pSpecular.g(), pSpecular.b(), pSpecular.a()),
                    new Vector4f(pEmissive.r(), pEmissive.g(), pEmissive.b(), pEmissive.a()),
                    pShininess[0],
                    pOpacity[0],
                    new Vector4f(pTransparent.r(), pTransparent.g(), pTransparent.b(), pTransparent.a()),
                    pAnisotropyFactor[0],
                    pRefractionIndex[0]
            );
        }
    }

    private static void processMeshes(final @NotNull AIScene pScene) {
        final var ppMeshes = pScene.mMeshes();
        assert ppMeshes != null;

        for (int i = 0; i < pScene.mNumMeshes(); ++i) {
            final var pMesh = AIMesh.create(ppMeshes.get(i));
            final var name  = pMesh.mName().dataString();

            final var vertices = new Vertex[pMesh.mNumVertices()];
            for (int j = 0; j < pMesh.mNumVertices(); ++j) {
                final var fj = j;

                final var position = Optional
                        .of(pMesh.mVertices())
                        .map(buffer -> buffer.get(fj))
                        .map(v -> new Vector3f(v.x(), v.y(), v.z()))
                        .orElseGet(Vector3f::new);
                final var normal = Optional
                        .ofNullable(pMesh.mNormals())
                        .map(buffer -> buffer.get(fj))
                        .map(v -> new Vector3f(v.x(), v.y(), v.z()))
                        .orElseGet(Vector3f::new);
                final var texture = Optional
                        .ofNullable(pMesh.mTextureCoords(0))
                        .map(buffer -> buffer.get(fj))
                        .map(v -> new Vector2f(v.x(), v.y()))
                        .orElseGet(Vector2f::new);
                final var color = Optional
                        .ofNullable(pMesh.mColors(0))
                        .map(buffer -> buffer.get(fj))
                        .map(v -> new Vector4f(v.r(), v.g(), v.b(), v.a()))
                        .orElseGet(() -> new Vector4f(1.0f));

                vertices[j] = new Vertex(position, texture, normal, color);
            }

            final var indices = new int[pMesh.mNumFaces() * 3];
            for (int j = 0; j < pMesh.mNumFaces(); ++j) {
                final var pFace = pMesh.mFaces().get(j);
                indices[j * 3] = pFace.mIndices().get(0);
                indices[j * 3 + 1] = pFace.mIndices().get(1);
                indices[j * 3 + 2] = pFace.mIndices().get(2);
            }

            final var info = new MeshInfo(name, vertices, indices);
            info.create();
        }
    }
}
