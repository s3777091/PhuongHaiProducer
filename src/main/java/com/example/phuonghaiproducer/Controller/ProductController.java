package com.example.phuonghaiproducer.Controller;


import com.example.phuonghaiproducer.Entity.MOrder;
import com.example.phuonghaiproducer.Entity.ProductCatogary;
import com.example.phuonghaiproducer.Entity.ProductEntity;
import com.example.phuonghaiproducer.Repository.CatogaryRepository;
import com.example.phuonghaiproducer.Repository.MoRepository;
import com.example.phuonghaiproducer.Repository.ProductRepository;
import com.example.phuonghaiproducer.Request.AddParent;
import com.example.phuonghaiproducer.Request.AddProductResquest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class ProductController {

    @Autowired
    KafkaTemplate<String, Object> controlTemplate;


    @Autowired
    ProductRepository productRepository;

    @Autowired
    CatogaryRepository catogaryRepository;

    @Autowired
    MoRepository moRepository;


    @GetMapping("/api/v1/get/list/catogory")
    public ResponseEntity<?> getListCatogary(@Param("type") String type) {
        return new ResponseEntity<>(catogaryRepository.findAll(), HttpStatus.OK);
    }

    @PostMapping("/api/v1/add/catogory")
    public CompletableFuture<ResponseEntity<?>> postProductToDb(@Param("type") String type) {

        ProductCatogary sths = catogaryRepository.findByProducttype(type);
        if (sths == null){
            ProductCatogary st = new ProductCatogary();
            st.setProducttype(type);
            controlTemplate.send("addCatogary",st);
            return CompletableFuture.completedFuture(new ResponseEntity<>("Success add Catogory", HttpStatus.OK));
        } else {
            return CompletableFuture.completedFuture(new ResponseEntity<>("Catogory already existing", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/api/v1/add/product")
    public CompletableFuture<ResponseEntity<?>> postProductToDb(@RequestBody AddProductResquest addProduct) {

        ProductEntity ts = productRepository.findByProductName(addProduct.getProductName());
        ProductCatogary sths = catogaryRepository.findByProducttype(addProduct.getType());

        if (sths == null){
            return CompletableFuture.completedFuture(new ResponseEntity<>("Product type is null", HttpStatus.FORBIDDEN));
        }

        if (ts == null){
            ProductEntity st = new ProductEntity();
            st.setProductName(addProduct.getProductName());
            st.setPrice(addProduct.getPrice());
            st.setFirstquality(addProduct.getQuality());
            st.setAvaiable(addProduct.getQuality());
            st.setTotalin("0");
            st.setTotalout("0");
            st.setProductDescription(addProduct.getProductDescription());
            st.setProductCatogary(sths);
            st.setParentname(addProduct.getParent());
            controlTemplate.send("addProduct", st);
            return CompletableFuture.completedFuture(new ResponseEntity<>("Success add Product", HttpStatus.OK));
        } else {
            return CompletableFuture.completedFuture(new ResponseEntity<>("Product already existing", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @GetMapping("/api/v1/view/product")
    public ResponseEntity<Object> getAllProduct(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @RequestParam(defaultValue = "All") String type) {
        Pageable paging = PageRequest.of(pageNo, pageSize);
        ProductCatogary prc = catogaryRepository.findByProducttype(type);
        if (!prc.getProducttype().equals(type)) {
            return new ResponseEntity<>("Can't find Product Type", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            Page<ProductEntity> listCatagory = productRepository.findAllByProductCatogary(paging, prc);
            return new ResponseEntity<>(listCatagory, new HttpHeaders(), HttpStatus.OK);
        }
    }

    @GetMapping("/api/v1/view/product/notype")
    public ResponseEntity<Object> getAllProductNotType(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "50") Integer pageSize) {

        Pageable paging = PageRequest.of(pageNo, pageSize);
        Page<ProductEntity> list = productRepository.findAll(paging);
            return new ResponseEntity<>(list, new HttpHeaders(), HttpStatus.OK);

    }

    @GetMapping("/api/v1/view/product_mo")
    public ResponseEntity<Object> getMpoProduct() {
        List<ProductEntity> list = productRepository.findAllByParentname("Parent");
        return new ResponseEntity<>(list, new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping("/api/v1/view/product_mo_conlections")
    public ResponseEntity<Object> getMpoLectionsProduct(@RequestParam(defaultValue = "") String name) {
        List<ProductEntity> list = productRepository.findAllByParentname(name);
        return new ResponseEntity<>(list, new HttpHeaders(), HttpStatus.OK);
    }


    @DeleteMapping("/api/v1/delete/product_mo")
    public ResponseEntity<Object> deleteMo(@RequestParam(defaultValue = "") long id) {
        MOrder s = moRepository.findById(id);
        controlTemplate.send("deleteMo", s);
        return new ResponseEntity<>("Success delete mo", new HttpHeaders(), HttpStatus.OK);
    }

}
