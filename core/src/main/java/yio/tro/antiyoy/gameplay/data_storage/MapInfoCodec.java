package yio.tro.antiyoy.gameplay.data_storage;

import com.badlogic.gdx.utils.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Codec for the description field of an exported map. The description is
 * compressed with GZIP and encoded with Base64 when it makes the payload
 * shorter, so it stays compact and ASCII-safe inside the level code.
 */
public class MapInfoCodec {

    private static final String COMPRESSED_PREFIX = "g:";
    private static final String RAW_PREFIX = "r:";


    public static String encode(String source) {
        if (source == null || source.length() == 0) return "";

        String compressed = compress(source);
        if (compressed.length() < source.length()) {
            return COMPRESSED_PREFIX + compressed;
        }

        return RAW_PREFIX + source;
    }


    public static String decode(String source) {
        if (source == null || source.length() == 0) return "";

        if (source.startsWith(COMPRESSED_PREFIX)) {
            return decompress(source.substring(COMPRESSED_PREFIX.length()));
        }

        if (source.startsWith(RAW_PREFIX)) {
            return source.substring(RAW_PREFIX.length());
        }

        // unknown/legacy payload: try to decompress, fall back to raw text
        String decompressed = tryDecompress(source);
        if (decompressed != null) return decompressed;
        return source;
    }


    private static String compress(String source) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream);
            gzipStream.write(source.getBytes(StandardCharsets.UTF_8));
            gzipStream.close();
            return new String(Base64Coder.encode(byteStream.toByteArray()));
        } catch (Exception e) {
            return source;
        }
    }


    private static String decompress(String source) {
        String result = tryDecompress(source);
        if (result != null) return result;
        return source;
    }


    private static String tryDecompress(String source) {
        try {
            byte[] bytes = Base64Coder.decode(source);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
            ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzipStream.read(buffer)) > 0) {
                resultStream.write(buffer, 0, length);
            }
            gzipStream.close();
            return new String(resultStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
