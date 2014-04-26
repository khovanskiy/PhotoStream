package ru.example.PhotoStream;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import ru.example.PhotoStream.Loaders.GroupsLoader;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.List;


public class GroupsFragment extends Fragment implements IEventHadler{

    static class GroupsAdapter extends BaseAdapter {

        private List<Group> groups = new ArrayList<>();
        private Context context;

        public GroupsAdapter(Context context)
        {
            this.context = context;
        }

        public void addGroup(Group group)
        {
            groups.add(group);
        }

        @Override
        public int getCount() {
            return groups.size();
        }

        @Override
        public Object getItem(int position) {
            return groups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Group group = groups.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.groupbadgeview, parent, false);
            SmartImage imageView = (SmartImage) view.findViewById(R.id.groupbadgeview_image);
            imageView.loadFromURL(group.photo.pic180min);
            TextView title = (TextView) view.findViewById(R.id.groupbadgeview_title);
            title.setText(group.name);
            TextView description = (TextView) view.findViewById(R.id.groupbadgeview_description);
            description.setText(group.description);
            return view;
        }
    }

    private Odnoklassniki api;
    private GroupsAdapter photoListAdapter;
    private GridView photoList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        api = Odnoklassniki.getInstance(getActivity());

        DataLoader loader = new GroupsLoader(api);
        loader.addEventListener(this);
        loader.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groupsactivity, container, false);
        photoList = (GridView) view.findViewById(R.id.groupsactivity_grouplist);
        return view;
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.COMPLETE)
        {
            e.target.removeEventListener(this);
            List<Group> groups = (List<Group>) e.data.get("groups");
            photoListAdapter = new GroupsAdapter(getActivity());
            photoList.setAdapter(photoListAdapter);

            for (Group group : groups)
            {
                Console.print(group.uid + " " + group.name);
                photoListAdapter.addGroup(group);
            }

            photoListAdapter.notifyDataSetChanged();
        }
    }
}
