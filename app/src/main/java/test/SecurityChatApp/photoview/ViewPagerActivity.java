/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package test.SecurityChatApp.photoview;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

import test.SecurityChatApp.R;
import test.SecurityChatApp.common.Util9;
import test.SecurityChatApp.model.ChatModel;

public class ViewPagerActivity extends AppCompatActivity {

	private static String roomID;
	private static String realname;
	private static ViewPager viewPager;
	private static ArrayList<String> imgList = new ArrayList<>();
    private String rootPath = Util9.getRootPath()+"/SecurityChatApp/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		roomID = getIntent().getStringExtra("roomID");
		realname = getIntent().getStringExtra("realname");

		viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(new SamplePagerAdapter());

        findViewById(R.id.downloadBtn).setOnClickListener(downloadBtnClickListener);
		//findViewById(R.id.rotateBtn).setOnClickListener(rotateBtnClickListener);

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setIcon(R.drawable.back);
        actionBar.setTitle("PhotoView");
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	Button.OnClickListener downloadBtnClickListener = new View.OnClickListener() {
		public void onClick(final View view) {
            if (!Util9.isPermissionGranted((Activity) view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                return ;
            }
            realname = imgList.get(viewPager.getCurrentItem());
            /// showProgressDialog("Downloading File.");
            FirebaseDatabase.getInstance().getReference().child("rooms").child(roomID).child("files").child(realname).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ChatModel.FileInfo fileInfo = dataSnapshot.getValue(ChatModel.FileInfo.class);

                    final File localFile = new File(rootPath, fileInfo.filename);

                    FirebaseStorage.getInstance().getReference().child("files/"+realname).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // hideProgressDialog();
                            Util9.showMessage(view.getContext(), "Downloaded file");
                            Log.e("SecurityChatApp ","local file created " +localFile.toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("SecurityChatApp ","local file not created  " +exception.toString());
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

		}
	};

    Button.OnClickListener rotateBtnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            View child = viewPager.getChildAt(viewPager.getCurrentItem());
            PhotoView photoView = child.findViewById(R.id.photoView);
            photoView.setRotation(photoView.getRotation()+90);
        }
    };

	static class SamplePagerAdapter extends PagerAdapter {
		private StorageReference storageReference;
		private int inx = -1;

		public SamplePagerAdapter() {
			storageReference  = FirebaseStorage.getInstance().getReference();

			FirebaseDatabase.getInstance().getReference().child("rooms").child(roomID).child("messages").addValueEventListener(new ValueEventListener(){
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					for (DataSnapshot item: dataSnapshot.getChildren()) {
						final ChatModel.Message message = item.getValue(ChatModel.Message.class);
						if ("1".equals(message.msgtype)) {
							imgList.add(message.msg);
							if (realname.equals(message.msg)) {inx = imgList.size()-1; }
						}
					}
					notifyDataSetChanged();
					if (inx>-1) {
						viewPager.setCurrentItem(inx);
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});
		}

		@Override
		public int getCount() {
			return imgList.size();
		}

		@Override
		public View instantiateItem(final ViewGroup container, final int position) {
			final PhotoView photoView = new PhotoView(container.getContext());
            photoView.setId(R.id.photoView);

			Glide.with(container.getContext())
					.load(storageReference.child("filesmall/"+imgList.get(position)))
					.into(photoView);

			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}
}
