package code.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;


import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.hathme.android.R;
import com.shockwave.pdfium.PdfDocument;

import java.io.File;
import java.util.List;

import code.utils.AppSettings;

public class ShowPdfActivity extends Activity implements OnPageChangeListener, OnLoadCompleteListener {

    ////------New PDFView implementation data ------------////
    private static final String TAG = ShowPdfActivity.class.getSimpleName();
    private String SAMPLE_FILE = "";
    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;
    File pdfFile;
    Bitmap myBitmap;
    Uri uri;
    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_pdf);
        SAMPLE_FILE = AppSettings.getString(AppSettings.KEY_selected_pdfurl);
        pdfView = findViewById(R.id.pdfView);
        pdfFile = new  File(AppSettings.getString(AppSettings.KEY_selected_pdfurl));
        uri = Uri.fromFile(pdfFile);
        displayFromAsset(SAMPLE_FILE);
        findViewById(R.id.imageback).setOnClickListener(view -> onBackPressed());
    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;
        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }
    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }
    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }
    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

}
