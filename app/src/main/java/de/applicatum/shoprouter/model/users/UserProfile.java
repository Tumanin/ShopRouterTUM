package de.applicatum.shoprouter.model.users;

import android.util.Log;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.TransactionalTask;
import com.couchbase.lite.UnsavedRevision;

import java.util.Date;
import java.util.List;

import de.applicatum.shoprouter.Application;

public class UserProfile {

    private String id;
    private String name;
    private Date banStart;
    private Date banEnd;
    private int rating;

    public UserProfile(String name){
        this.name = name;
        rating = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBanStart() {
        return banStart;
    }

    public void setBanStart(Date banStart) {
        this.banStart = banStart;
    }

    public Date getBanEnd() {
        return banEnd;
    }

    public void setBanEnd(Date banEnd) {
        this.banEnd = banEnd;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public static boolean migrateGuestData(final Database guestDb, final Document profile) {
        boolean success = true;
        final Database userDB = profile.getDatabase();
        if (guestDb.getLastSequenceNumber() > 0 && userDB.getLastSequenceNumber() == 0) {
            success = userDB.runInTransaction(new TransactionalTask() {
                @Override
                public boolean run() {
                    try {
                        QueryEnumerator rows = guestDb.createAllDocumentsQuery().run();
                        for (QueryRow row : rows) {
                            Document doc = row.getDocument();
                            Document newDoc = userDB.getDocument(doc.getId());
                            newDoc.putProperties(doc.getUserProperties());

                            List<Attachment> attachments = doc.getCurrentRevision().getAttachments();
                            if (attachments.size() > 0) {
                                UnsavedRevision rev = newDoc.getCurrentRevision().createRevision();
                                for (Attachment attachment : attachments) {
                                    rev.setAttachment(
                                            attachment.getName(),
                                            attachment.getContentType(),
                                            attachment.getContent());
                                }
                                rev.save();
                            }
                        }
                        // Delete guest database:
                        guestDb.delete();
                    } catch (CouchbaseLiteException e) {
                        Log.e(Application.TAG, "Error when migrating guest data to user", e);
                        return false;
                    }
                    return true;
                }
            });
        }
        return success;
    }
}
