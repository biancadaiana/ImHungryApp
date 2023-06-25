package com.dev.imhungryapp.Filters;

import android.widget.Filter;

import com.dev.imhungryapp.Adapters.AdapterProductAdmin;
import com.dev.imhungryapp.Models.ModelProduct;

import java.util.ArrayList;

public class FilterProduct extends Filter {

    private AdapterProductAdmin adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProduct(AdapterProductAdmin adapter, ArrayList<ModelProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if(constraint != null && constraint.length() > 0){
            //search file no empty, searching something, perform search
            //change to upper case, to make vcase sensitive
            constraint= constraint.toString().toUpperCase();
            //store out filtered list
            ArrayList<ModelProduct> filteredModels= new ArrayList<>();

            for(int i=0; i<filterList.size(); i++){
                //check, serach by title and category
                if(filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getProductCategory().toUpperCase().contains(constraint))
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
        adapter.productList = (ArrayList<ModelProduct>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
