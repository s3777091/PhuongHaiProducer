package com.example.phuonghaiproducer.Controller;

import com.example.phuonghaiproducer.Entity.MOrder;
import com.example.phuonghaiproducer.Entity.ProductEntity;
import com.example.phuonghaiproducer.Repository.MoRepository;
import com.example.phuonghaiproducer.Repository.ProductRepository;
import com.example.phuonghaiproducer.Request.AddEditingMo;
import com.example.phuonghaiproducer.Request.AddMo;
import com.example.phuonghaiproducer.Request.AddNewsBill;
import com.example.phuonghaiproducer.Request.addFilterInventory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class MoController {

    @Autowired
    KafkaTemplate<String, Object> controlTemplate;

    @Autowired
    MoRepository moRepository;

    @Autowired
    ProductRepository productRepository;

    //Filter
    @GetMapping("/api/v1/get/mo")
    public ResponseEntity<?> getMoList() {
        return new ResponseEntity<>(moRepository.findAll(), HttpStatus.OK);
    }

    @PostMapping("/api/v1/add/mo")
    public ResponseEntity<?> AddNewsMo(@RequestBody AddMo addMo) {
        //Checking product
        ProductEntity s = productRepository.findByProductName(addMo.getProduct());

        MOrder m = moRepository.findByControltoken(addMo.getToken());
        if (s == null) {
            return new ResponseEntity<>("Product not existing in system", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (m == null) {
            List<ProductEntity> listChild = productRepository.findAllByParentname(addMo.getProduct());

            listChild.forEach(e -> {
                e.setTotalin(String.valueOf(Integer.parseInt(e.getTotalin()) + 1));
                e.setAvaiable(String.valueOf((Integer.parseInt(e.getTotalin())
                        + Integer.parseInt(e.getFirstquality())) - Integer.parseInt(e.getTotalout())));
                productRepository.save(e);
            });
            //Create first order
            MOrder fm = new MOrder();
            fm.setControltoken(addMo.getToken());
            fm.setUserName(addMo.getUserName());
            //Date Start
            fm.setDateStartMo(new Timestamp(System.currentTimeMillis()));
            //Date End
            fm.setDateDelivery(new Timestamp(System.currentTimeMillis() + 13L * 24 * 60 * 60 * 1000));
            //Date expection is more 12 days from start
            fm.setDateExpectedCompletion(new Timestamp(System.currentTimeMillis() + 12L * 24 * 60 * 60 * 1000));
            Map<String, String> listC = new HashMap<>();
            listC.put(addMo.getProduct(), "1");
            fm.setCollection(listC);
            controlTemplate.send("addMo", fm);
            return new ResponseEntity<>("Success add New Mpo", HttpStatus.OK);
        } else {
            List<ProductEntity> listChild = productRepository.findAllByParentname(addMo.getProduct());

            listChild.forEach(e -> {
                e.setTotalin(String.valueOf(Integer.parseInt(e.getTotalin()) + 1));
                e.setAvaiable(String.valueOf((Integer.parseInt(e.getTotalin())
                        + Integer.parseInt(e.getFirstquality())) - Integer.parseInt(e.getTotalout())));
                controlTemplate.send("addMo", e);
            });
            Map<String, String> listC = m.getCollection();
            try {
                listC.put(addMo.getProduct(), String.valueOf(Integer.parseInt(listC.get(addMo.getProduct())) + 1));
                m.setCollection(listC);
                controlTemplate.send("addMo", m);
            } catch (Exception e) {
                listC.put(addMo.getProduct(), "1");
                m.setCollection(listC);
                controlTemplate.send("addMo", m);
            }
            return new ResponseEntity<>("Success add Mpo", HttpStatus.OK);
        }
    }

    private long returnTime(String date) {
        try {
            Date ExDate = new SimpleDateFormat("yy-MM-dd").parse(date);
            return ExDate.getTime();
        } catch (Exception e) {
            System.out.println(e);
        }
        return 0;
    }

    @PostMapping("/api/editing/mo")
    public ResponseEntity<?> EditingMo(@RequestParam(defaultValue = "") long id, @RequestBody AddEditingMo addEditingMo)
            throws Exception {
        MOrder s = moRepository.findById(id);
        if (s == null) {
            return new ResponseEntity<>("Can't find Mo", HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            s.setUserName(addEditingMo.getNewUser());
            s.setDateExpectedCompletion(new Timestamp(returnTime(addEditingMo.getDateExpectation())));
            s.setDateStartMo(new Timestamp(returnTime(addEditingMo.getDateStart())));
            controlTemplate.send("EditMo", s);
            return new ResponseEntity<>("Success change date", HttpStatus.OK);
        }
    }


    @GetMapping("/api/get/mpo")
    public ResponseEntity<?> getBill(@RequestParam(defaultValue = "") long id) {
        AtomicInteger bill = new AtomicInteger();
        JSONObject total_pr = new JSONObject();
        JSONArray tsa = new JSONArray();
        MOrder s = moRepository.findById(id);
        if (s == null) {
            total_pr.put("success", false);
            return new ResponseEntity<>(total_pr.toMap(), HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            total_pr.put("success", true);
            Map<String, String> listC = s.getCollection();
            for (Map.Entry<String, String> entry : listC.entrySet()) {
                String pr_name = entry.getKey();
                String pr_quality = entry.getValue();
                List<ProductEntity> dslist = productRepository.findAllByParentname(pr_name);

                dslist.forEach(e -> {
                    JSONObject itemProduct = new JSONObject();
                    itemProduct.put("components_name", e.getProductName());
                    itemProduct.put("components_price", e.getPrice());
                    itemProduct.put("components_quality", pr_quality);
                    tsa.put(itemProduct);
                    bill.addAndGet(Integer.parseInt(pr_quality) * Integer.parseInt(e.getPrice()));
                });

                total_pr.put("results", tsa);
            }

            total_pr.put("total_bill", bill.get());
            return new ResponseEntity<>(total_pr.toMap(), HttpStatus.OK);
        }
    }

    @GetMapping("/api/get/bill")
    public ResponseEntity<?> getBillsByType(@RequestParam(defaultValue = "") long id) {
        MOrder m = moRepository.findById(id);
        return new ResponseEntity<>(m.getCollection(), HttpStatus.OK);
    }


    @GetMapping("/api/get/bill/components")
    public ResponseEntity<?> getComponents() {
        JSONObject total_pr = new JSONObject();
        JSONArray tsa = new JSONArray();
        List<ProductEntity> st = productRepository.findAll();
        st.forEach(e -> {
            JSONObject itemProduct = new JSONObject();
            if (Objects.equals(e.getParentname(), "Parent")) {
                itemProduct.put("name", e.getProductName());
                itemProduct.put("price", e.getPrice());
                itemProduct.put("type", "product");
                tsa.put(itemProduct);
            } else {
                itemProduct.put("name", e.getProductName());
                itemProduct.put("price", e.getPrice());
                itemProduct.put("type", "components");
                tsa.put(itemProduct);
            }
            total_pr.put("results", tsa);
        });
        return new ResponseEntity<>(total_pr.toMap(), HttpStatus.OK);
    }

    @PostMapping("/api/add/bill/components")
    public ResponseEntity<?> addComponents(@RequestBody AddNewsBill addNewsBill) {
        MOrder s = moRepository.findById(Long.parseLong(addNewsBill.getId()));
        if (s == null) {
            return new ResponseEntity<>("Fails", HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            Map<String, String> listC = s.getCollection();
            try {
                listC.put(addNewsBill.getName(), String.valueOf(Integer.parseInt(listC.get(addNewsBill.getName())) + 1));
                s.setCollection(listC);
                controlTemplate.send("addBill", s);
            } catch (Exception e) {
                listC.put(addNewsBill.getName(), "1");
                s.setCollection(listC);
                controlTemplate.send("addBill", s);
            }
            return new ResponseEntity<>("success add more components", HttpStatus.OK);
        }
    }


    @PostMapping("/api/filter_by_date")
    public ResponseEntity<?> filter(@RequestBody addFilterInventory ad) {

        JSONObject total_pr = new JSONObject();
        JSONArray tsa = new JSONArray();

        List<MOrder> ts = moRepository.findAll();
        long userStartDate = Long.parseLong(String.valueOf(returnTime(ad.getDateStart())));
        long userEndDate = Long.parseLong(String.valueOf(returnTime(ad.getDateEnd())));
        ts.forEach(e -> {
            long time_start = Long.parseLong(String.valueOf(returnTime(String.valueOf(e.getDateStartMo()))));
            long time_end = Long.parseLong(String.valueOf(returnTime(String.valueOf(e.getDateDelivery()))));
            if (userStartDate < time_start && time_start < userEndDate) {
                Map<String, String> listC = e.getCollection();

                for (String key : listC.keySet()) {
                    ProductEntity ps = productRepository.findByProductName(key);
                    List<ProductEntity> tsf = productRepository.findAllByParentname(key);

                    JSONObject itemProducts = new JSONObject();
                    itemProducts.put("type", "product");
                    itemProducts.put("product_name", ps.getProductName());
                    itemProducts.put("avaiable", ps.getAvaiable());
                    itemProducts.put("firt_quality", ps.getFirstquality());
                    itemProducts.put("total_out", ps.getTotalout());
                    itemProducts.put("total_in", ps.getTotalin());
                    tsa.put(itemProducts);
                    total_pr.put("results", tsa);

                    tsf.forEach(es -> {
                        JSONObject itemProductl = new JSONObject();
                        itemProductl.put("type", "components");
                        itemProductl.put("product_name", es.getProductName());
                        itemProductl.put("avaiable", es.getAvaiable());
                        itemProductl.put("firt_quality", es.getFirstquality());
                        itemProductl.put("total_out", es.getTotalout());
                        itemProductl.put("total_in", es.getTotalin());
                        tsa.put(itemProductl);
                        total_pr.put("results", tsa);
                    });
                }
            }

            if (userStartDate < time_end && time_end < userEndDate) {
                Map<String, String> listC = e.getCollection();
                for (String key : listC.keySet()) {
                    List<ProductEntity> tsf = productRepository.findAllByParentname(key);
                    ProductEntity ps = productRepository.findByProductName(key);
                    JSONObject itemProductk = new JSONObject();
                    int outs = Integer.parseInt(ps.getTotalout()) + Integer.parseInt(ps.getTotalin());
                    int avaiables =
                            (Integer.parseInt(ps.getFirstquality()) + Integer.parseInt(ps.getTotalin())) - outs;
                    itemProductk.put("type", "product");
                    itemProductk.put("product_name", ps.getProductName());
                    itemProductk.put("avaiable", avaiables);
                    itemProductk.put("firt_quality", ps.getFirstquality());
                    itemProductk.put("total_out", outs);
                    itemProductk.put("total_in", String.valueOf((Integer.parseInt(ps.getAvaiable()) - outs) - Integer.parseInt(ps.getFirstquality())));
                    tsa.put(itemProductk);
                    total_pr.put("results", tsa);

                    tsf.forEach(es -> {
                        JSONObject itemProductf = new JSONObject();
                        int out = Integer.parseInt(es.getTotalout()) + Integer.parseInt(es.getTotalin());
                        int avaiable =
                                (Integer.parseInt(es.getFirstquality()) + Integer.parseInt(es.getTotalin())) - out;
                        itemProductf.put("type", "components");
                        itemProductf.put("product_name", es.getProductName());
                        itemProductf.put("avaiable", avaiable);
                        itemProductf.put("firt_quality", es.getFirstquality());
                        itemProductf.put("total_out", out);
                        itemProductf.put("total_in", String.valueOf((Integer.parseInt(es.getAvaiable()) - out) - Integer.parseInt(es.getFirstquality())));
                        tsa.put(itemProductf);
                        total_pr.put("results", tsa);
                    });
                }
            }
        });
        return new ResponseEntity<>(total_pr.toMap(), HttpStatus.OK);
    }

}
