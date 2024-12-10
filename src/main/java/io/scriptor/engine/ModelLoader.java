package io.scriptor.engine;

import io.scriptor.engine.data.MaterialInfo;
import io.scriptor.engine.data.MeshInfo;
import io.scriptor.engine.data.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.assimp.*;

import java.io.InputStream;

import static org.lwjgl.assimp.Assimp.*;

public class ModelLoader {

    private ModelLoader() {
    }

    public static void loadModel(final InputStream stream) {
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
        final var pScene = aiImportFileEx(path, flags, resources.getFileIO());
        assert pScene != null;

        processMaterials(pScene);
        processMeshes(pScene);
    }

    private static void processMaterials(final AIScene pScene) {
        final var ppMaterials = pScene.mMaterials();
        assert ppMaterials != null;
        for (int i = 0; i < pScene.mNumMaterials(); ++i) {
            final var pMaterial = AIMaterial.create(ppMaterials.get(i));

            final var pName = AIString.create();
            final var pShadingMode = new int[1];
            final var pIllumination = new int[1];
            final var pAmbient = AIColor4D.create();
            final var pDiffuse = AIColor4D.create();
            final var pSpecular = AIColor4D.create();
            final var pEmissive = AIColor4D.create();
            final var pShininess = new float[1];
            final var pOpacity = new float[1];
            final var pTransparent = AIColor4D.create();
            final var pAnisotropyFactor = new float[1];
            final var pRefractionIndex = new float[1];

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
            System.out.println(info);
        }
    }

    private static void processMeshes(final AIScene pScene) {
        final var ppMeshes = pScene.mMeshes();
        assert ppMeshes != null;
        for (int i = 0; i < pScene.mNumMeshes(); ++i) {
            final var pMesh = AIMesh.create(ppMeshes.get(i));

            final var name = pMesh.mName().dataString();

            final var vertices = new Vertex[pMesh.mNumVertices()];
            for (int j = 0; j < pMesh.mNumVertices(); ++j) {
                final var position = pMesh.mVertices().get(j);
                final var normal = pMesh.mNormals().get(j);
                final var texture = pMesh.mTextureCoords(0).get(j);
                final var color = pMesh.mColors(0) != null ? pMesh.mColors(0).get(j) : null;

                vertices[j] = new Vertex(
                        new Vector3f(position.x(), position.y(), position.z()),
                        new Vector2f(texture.x(), texture.y()),
                        new Vector3f(normal.x(), normal.y(), normal.z()),
                        color != null
                                ? new Vector4f(color.r(), color.g(), color.b(), color.a())
                                : new Vector4f(1.0f)
                );
            }

            final var indices = new int[pMesh.mNumFaces() * 3];
            for (int j = 0; j < pMesh.mNumFaces(); ++j) {
                final var pFace = pMesh.mFaces().get(j);
                indices[j * 3] = pFace.mIndices().get(0);
                indices[j * 3 + 1] = pFace.mIndices().get(1);
                indices[j * 3 + 2] = pFace.mIndices().get(2);
            }

            final var info = new MeshInfo(name, vertices, indices);
            System.out.println(info);

            info.create();
        }
    }
}
