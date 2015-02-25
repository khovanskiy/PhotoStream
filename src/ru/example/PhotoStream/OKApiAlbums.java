package ru.example.PhotoStream;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.example.PhotoStream.Activities.UIActivity;

import java.util.ArrayList;
import java.util.List;

public class OKApiAlbums extends OKApiBase {
    public OKRequest get(final User owner, final OKParameters params) {
        return new OKRequest("photos.getAlbums", params) {
            @Override
            protected void start() {
                /*Album personalAlbum = Album.build("personal" + objectId, objectId, AlbumType.USER);
                personalAlbum.title = "Личный альбом";
                personalAlbum.isPersonal = true;
                albums.add(personalAlbum);*/
                provideResponse(albumsInfo(AlbumType.USER, owner.getId(), params));
            }
        };
    }

    public OKRequest get(final Group owner, final OKParameters params) {
        return new OKRequest("photos.getAlbums", params) {
            @Override
            protected void start() {
                /*Album personalAlbum = Album.build("personal" + objectId, objectId, AlbumType.USER);
                personalAlbum.title = "Личный альбом";
                personalAlbum.isPersonal = true;
                albums.add(personalAlbum);*/
                provideResponse(albumsInfo(AlbumType.GROUP, owner.getId(), params));
            }
        };
    }

    public OKRequest get(final Selection owners, final OKParameters params) {
        return new OKRequest("photos.getAlbums", params) {
            @Override
            protected void start() {
                /*Album personalAlbum = Album.build("personal" + objectId, objectId, AlbumType.USER);
                personalAlbum.title = "Личный альбом";
                personalAlbum.isPersonal = true;
                albums.add(personalAlbum);*/
                for (final AlbumsOwner owner : owners.getOwners()) {
                    if (owner instanceof User) {
                        provideResponse(albumsInfo(AlbumType.USER, owner.getId(), params));
                    } else if (owner instanceof Group) {
                        provideResponse(albumsInfo(AlbumType.GROUP, owner.getId(), params));
                    }
                }
            }
        };
    }

    private static List<Album> albumsInfo(final AlbumType albumType, final String ownerId, final OKParameters params) {
        OKParameters requestParams = new OKParameters(params);
        if (albumType == AlbumType.USER) {
            requestParams.put("fid", ownerId);
        } else if (albumType == AlbumType.GROUP) {
            requestParams.put("gid", ownerId);
        }
        List<Album> albums = new ArrayList<>();
        boolean hasMore = true;
        while (hasMore) {
            try {
                String response = UIActivity.getAPI().request("photos.getAlbums", requestParams, "post");
                JSONObject albumsObject = new JSONObject(response);
                JSONArray array = albumsObject.getJSONArray("albums");
                for (int i = 0; i < array.length(); ++i) {
                    Album album = Album.build(array.getJSONObject(i));
                    albums.add(album);
                }
                hasMore = albumsObject.getBoolean("hasMore");
                requestParams.put("pagingAnchor", albumsObject.getString("pagingAnchor"));
            } catch (Exception e) {
                hasMore = false;
            }
        }
        return albums;
    }
}
