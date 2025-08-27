package com.example.springbootapp.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springbootapp.dto.SingleDayListRow;
import com.example.springbootapp.entity.ExternalHotel;
import com.example.springbootapp.repository.ExternalHotelRepo;
import com.example.springbootapp.repository.PriceSnapshotRepo;
import com.example.springbootapp.repository.PriceTableExternalHotelRepo;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class PriceTableUiService {
  private final PriceTableExternalHotelRepo tableHotels;
  private final ExternalHotelRepo hotels;
  private final PriceSnapshotRepo snapshots;

  

  public List<SingleDayListRow> singleDayList(Long priceTableId, LocalDate date) {
    
    var links = tableHotels.findByIdPriceTableId(priceTableId);
    var result = new ArrayList<SingleDayListRow>();

    for (var link : links) {
      Long hotelRef = link.getId().getExternalHotelRef();
      ExternalHotel h = hotels.findById(hotelRef).orElse(null);
      if (h == null) continue;

      var snap = snapshots.findByProviderAndExternalHotelIdAndCheckinDateAndCheckoutDate(
          h.getProvider(), h.getExternalHotelId(), date, date.plusDays(1))
          .stream().reduce((a,b)->b).orElse(null);

      result.add(new SingleDayListRow(
          priceTableId,
          h.getExternalHotelId(),
          h.getNameCached(),
          snap != null ? snap.getPriceTotal() : null,
          link.isOwner()   // bold in UI
      ));
    }
    return result;
  }
}