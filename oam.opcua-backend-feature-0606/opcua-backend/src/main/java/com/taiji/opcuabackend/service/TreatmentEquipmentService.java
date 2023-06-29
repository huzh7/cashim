package com.taiji.opcuabackend.service;

import com.taiji.opcuabackend.entity.TreatmentEquipment;

import java.util.List;

public interface TreatmentEquipmentService {


    public List<TreatmentEquipment> getTreatmentEquipment(TreatmentEquipment treatmentEquipment) throws Exception;

}
