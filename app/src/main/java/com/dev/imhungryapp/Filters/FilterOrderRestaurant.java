package com.dev.imhungryapp.Filters;

import android.widget.Filter;

import com.dev.imhungryapp.Adapters.AdapterOrderRestaurant;
import com.dev.imhungryapp.Models.ModelOrderRestaurant;

import java.util.ArrayList;

public class FilterOrderRestaurant extends Filter {

    private AdapterOrderRestaurant adapter;
    private ArrayList<ModelOrderRestaurant> filterList;

    public FilterOrderRestaurant(AdapterOrderRestaurant adapter, ArrayList<ModelOrderRestaurant> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if(constraint != null && constraint.length() > 0){
            //search file no empty, searching something, perform search
            //change to upper case, to make case sensitive
            constraint= constraint.toString().toUpperCase();
            //store out filtered list
            ArrayList<ModelOrderRestaurant> filteredModels= new ArrayList<>();

            for(int i=0; i<filterList.size(); i++){
                //check, serach by title and category
                if(filterList.get(i).getOrderStatus().toUpperCase().contains(constraint))
                {
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));
                }

            }
            results.count= filteredModels.size();
            results.values= filteredModels;
        }else {
            //search file no empty, not searching, return original/all/complete list
            results.count= filterList.size();
            results.values= filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.orderRestaurantList = (ArrayList<ModelOrderRestaurant>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
