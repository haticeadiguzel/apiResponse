package api.response.apiResponse.business.concretes;

import api.response.apiResponse.business.DTOs.Responses.GetAllUrls;
import api.response.apiResponse.business.abstracts.ModelService;
import api.response.apiResponse.core.utilities.mappers.ModelMapperService;
import api.response.apiResponse.dataAccess.abstracts.ModelRepository;
import api.response.apiResponse.entities.concretes.Model;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.whois.WhoisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ModelManager implements ModelService {
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private ModelMapperService modelMapperService;

    @Override
    public List<GetAllUrls> getAllUrls() {
        List<Model> models = modelRepository.findAll();

        List<GetAllUrls> urlsResponse = models.stream()
                .map(url -> this.modelMapperService.forResponse().map(url, GetAllUrls.class))
                .collect(Collectors.toList());

        ArrayList<String> urls = new ArrayList<>();
        for (GetAllUrls url : urlsResponse) {
            urls.add(url.getUrl());
        }
        ArrayList<String> newUrls = removeDuplicates(urls);

        ModelManager obj = new ModelManager();

        log.info("Urls: " + newUrls);
        log.info("Number of urls: " + newUrls.size());

        for (String url : newUrls) {
            System.out.println(obj.crunchifyWhois(url));
        }
        return urlsResponse;
    }

    @Override
    public <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        ArrayList<T> newList = new ArrayList<T>();
        for (T element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    @Override
    public String crunchifyWhois(String url) {
        StringBuilder whoisResult = new StringBuilder("");
        WhoisClient crunchifyWhois = new WhoisClient();
        try {
            crunchifyWhois.connect(WhoisClient.DEFAULT_HOST);
            String whoisData = crunchifyWhois.query("=" + url);
            whoisResult.append(whoisData);
            crunchifyWhois.disconnect();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return whoisResult.toString();
    }
}
