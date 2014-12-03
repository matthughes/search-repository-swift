package org.wikimedia.elasticsearch.swift.repositories.blobstore;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.support.AbstractBlobContainer;
import org.elasticsearch.common.blobstore.support.PlainBlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.javaswift.joss.model.Directory;
import org.javaswift.joss.model.DirectoryOrObject;
import org.javaswift.joss.model.StoredObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Swift's implementation of the AbstractBlobContainer
 */
public class AbstractSwiftBlobContainer extends AbstractBlobContainer {
	// Our local swift blob store instance
    protected final SwiftBlobStore blobStore;

    // The root path for blobs. Used by buildKey to build full blob names
    protected final String keyPath;

    /**
     * Constructor
     * @param path The BlobPath to find blobs in
     * @param blobStore The blob store to use for operations
     */
    protected AbstractSwiftBlobContainer(BlobPath path, SwiftBlobStore blobStore) {
        super(path);
        this.blobStore = blobStore;
        String keyPath = path.buildAsString("/");
        if (!keyPath.isEmpty()) {
            keyPath = keyPath + "/";
        }
        this.keyPath = keyPath;
    }

    /**
     * Does a blob exist? Self-explanatory.
     */
    @Override
    public boolean blobExists(String blobName) {
    	return blobStore.swift().getObject(buildKey(blobName)).exists();
    }

    /**
     * Read a given blob into the listener
     * @param blobName The blob name to read
     * @param listener The listener to report our read info back to
     */
    @Override
    public void readBlob(final String blobName, final ReadBlobListener listener) {
        blobStore.executor().execute(new Runnable() {
            @Override
            public void run() {
                InputStream is;
                try {
                	// This is the interesting bit. Fetch the blob and then turn it
                	// into an InputStream for reading below
                    StoredObject object = blobStore.swift().getObject(buildKey(blobName));
                    is = object.downloadObjectAsInputStream();
                } catch (Exception e) {
                    listener.onFailure(e);
                    return;
                }
                byte[] buffer = new byte[blobStore.bufferSizeInBytes()];
                try {
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        listener.onPartial(buffer, 0, bytesRead);
                    }
                    listener.onCompleted();
                } catch (Exception e) {
                    try {
                        is.close();
                    } catch (IOException e1) {
                        // ignore
                    }
                    listener.onFailure(e);
                }
            }
        });
    }

    /**
     * Delete a blob. Straightforward.
     * @param blobName A blob to delete
     */
    @Override
    public boolean deleteBlob(String blobName) throws IOException {
    	StoredObject object = blobStore.swift().getObject(buildKey(blobName));
    	if (object.exists()) {
    		object.delete();
    	}
        return true;
    }

    /**
     * Get the blobs matching a given prefix
     * @param blobNamePrefix The prefix to look for blobs with
     */
    @Override
    public ImmutableMap<String, BlobMetaData> listBlobsByPrefix(@Nullable String blobNamePrefix) throws IOException {
        ImmutableMap.Builder<String, BlobMetaData> blobsBuilder = ImmutableMap.builder();

        final Collection<DirectoryOrObject> files = blobStore.swift().listDirectory(new Directory(keyPath, '/'));
        if (files != null && !files.isEmpty()) {
            for (DirectoryOrObject object : files) {
                if (object.isObject()) {
                    final String name = object.getName().substring(keyPath.length());
                    if (blobNamePrefix == null || name.startsWith(blobNamePrefix)) {
                        blobsBuilder.put(name, new PlainBlobMetaData(name, object.getAsObject().getContentLength()));
                    }
                }
            }
        }

        return blobsBuilder.build();
    }

    /**
     * Get all the blobs
     */
    @Override
    public ImmutableMap<String, BlobMetaData> listBlobs() throws IOException {
        return listBlobsByPrefix(null);
    }

    /**
     * Build a key for a blob name, based on the keyPath
     * @param blobName The blob name to build a key for
     */
    protected String buildKey(String blobName) {
        return keyPath + blobName;
    }
}
