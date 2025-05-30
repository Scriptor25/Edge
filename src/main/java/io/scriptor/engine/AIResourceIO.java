package io.scriptor.engine;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.assimp.AIFile;
import org.lwjgl.assimp.AIFileIO;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static io.scriptor.engine.data.Resources.open;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryUtil.*;

public class AIResourceIO implements IDestructible {

    private final @NotNull AIFileIO aiFileIO;
    private final @NotNull Map<AIFile, @NotNull ByteBuffer> aiFiles = new HashMap<>();

    public AIResourceIO() {
        aiFileIO = AIFileIO.create();
        aiFileIO.OpenProc((pFileIO, pFilename, pOpenMode) -> {
            final var filename = memASCII(pFilename);

            return open(filename, stream -> {
                final var data = stream.readAllBytes();

                final var buffer = memAlloc(data.length)
                        .put(data)
                        .flip();

                final var aiFile = AIFile.create();
                aiFile.ReadProc((pFile, pBuffer, size, count) -> {
                    final var bytes = Math.min(buffer.remaining(), size * count);
                    memCopy(memAddress(buffer), pBuffer, bytes);
                    buffer.position(buffer.position() + (int) bytes);
                    return bytes / size;
                });
                aiFile.SeekProc((pFile, offset, origin) -> {
                    switch (origin) {
                        case aiOrigin_CUR:
                            buffer.position(buffer.position() + (int) offset);
                            break;
                        case aiOrigin_SET:
                            buffer.position((int) offset);
                            break;
                        case aiOrigin_END:
                            buffer.position(buffer.limit() + (int) offset);
                            break;
                        default:
                            return 1;
                    }
                    return 0;
                });
                aiFile.FileSizeProc(pFile -> buffer.limit());

                aiFiles.put(aiFile, buffer);
                return aiFile.address();
            }).or(NULL);
        });
        aiFileIO.CloseProc((pFileIO, pFile) -> {
            if (pFile == NULL)
                return;
            final var aiFile = AIFile.create(pFile);
            memFree(aiFiles.get(aiFile));
        });
    }

    public @NotNull AIFileIO getFileIO() {
        return aiFileIO;
    }

    @Override
    public void destroy() {
        aiFileIO.close();
    }
}
