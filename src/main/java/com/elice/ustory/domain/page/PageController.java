package com.elice.ustory.domain.page;

import com.elice.ustory.domain.page.dto.AddPageRequest;
import com.elice.ustory.domain.page.dto.AddPageResponse;
import com.elice.ustory.domain.page.dto.PageListResponse;
import com.elice.ustory.domain.page.dto.PageResponse;
import com.elice.ustory.domain.page.dto.UpdatePageRequest;
import com.elice.ustory.domain.page.dto.UpdatePageResponse;
import com.elice.ustory.domain.page.entity.Address;
import com.elice.ustory.domain.page.entity.Image;
import com.elice.ustory.domain.page.entity.Page;
import com.elice.ustory.domain.page.service.AddressService;
import com.elice.ustory.domain.page.service.ImageService;
import com.elice.ustory.domain.page.service.PageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Page API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PageController {

    private final AddressService addressService;
    private final PageService pageService;
    private final ImageService imageService;

    @Operation(summary = "Create Page API", description = "페이지를 생성한다.")
    @PostMapping("/page")
    public ResponseEntity<AddPageResponse> create(@RequestBody AddPageRequest addPageRequest) {

        // 사용자 검증 메서드

        // 다이어리 검증 메서드

        Address address = addressService.createAddress(addPageRequest.toAddressEntity());

        List<Image> images = imageService.createImages(addPageRequest.toImagesEntity());

        Page page = pageService.createPage(addPageRequest.toPageEntity(), images, address);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AddPageResponse(page.getId()));
    }

    @Operation(summary = "Update Page API", description = "페이지를 수정한다.")
    @PutMapping("/page/{pageId}")
    public ResponseEntity<UpdatePageResponse> update(@PathVariable Long pageId,
                                                     @RequestBody UpdatePageRequest updatePageRequest) {

        // 다이어리 검증 메서드 (다이어리에 해당 페이지가 존재하는지 확인)

        // 사용자 검증 메서드 (사용자가 존재하는지, 다이어리에 포함되는지 확인)

        Page page = pageService.getPageById(pageId);

        Address address = addressService.updateAddress(page.getAddress().getId(), updatePageRequest.toAddressEntity());

        // 이미지 업데이트 시, 삭제되거나 추가되는 이미지들이 존재한다.
        // 삭제되는 이미지 존재시 해당 이미지를 삭제하고, 추가되는 이미지를 추가해야 한다.
        // 이를 어떻게 구현할지 생각해봐야 한다.

        pageService.updatePage(page);

        return ResponseEntity.ok(new UpdatePageResponse());
    }

    @Operation(summary = "Read Page API", description = "페이지를 불러온다.")
    @GetMapping("/page/{pageId}")
    public ResponseEntity<PageResponse> read(@PathVariable Long pageId) {

        Page page = pageService.getPageById(pageId);

        return ResponseEntity.ok(new PageResponse(page));
    }

    @Operation(summary = "Read Pages API", description = "모든 페이지를 불러온다.</br>(우선 사용되지 않을 API)</br>사용된다면 관리자 페이지에서 사용될 듯 함")
    @GetMapping("/pages")
    public ResponseEntity<List<PageListResponse>> getAllPages(@RequestParam(name = "page", defaultValue = "1") int page,
                                                             @RequestParam(name = "size", defaultValue = "20") int size) {

        List<PageListResponse> pages = pageService.getAllPages().stream()
                .map(PageListResponse::new)
                .toList();

        return ResponseEntity.ok(pages);
    }

    // TODO: userId를 임시로 작성해놨지만 변경 해야함
    @Operation(summary = "Read Pages By User API", description = "유저와 연관된 모든 페이지를 불러온다.")
    @GetMapping(value = "/pages/user", params = "userId")
    public ResponseEntity<List<AddPageResponse>> getAllPagesByUser(@RequestParam(name = "userId") Long userId,
                                                                   @RequestParam(name = "page", defaultValue = "1") int page,
                                                                   @RequestParam(name = "size", defaultValue = "20") int size) {

        // user 검증
        // user 연관된 모든 page 불러오기

        return ResponseEntity.ok(List.of(new AddPageResponse()));
    }

    @Operation(summary = "Read Pages By Diary API", description = "다이어리와 연관된 모든 페이지를 불러온다.")
    @GetMapping(value = "/pages/diary", params = "diaryId")
    public ResponseEntity<List<AddPageResponse>> getAllPagesByDiary(@RequestParam(name = "diaryId") Long diaryId,
                                                                    @RequestParam(name = "page", defaultValue = "1") int page,
                                                                    @RequestParam(name = "size", defaultValue = "20") int size) {

        // diary 검증
        // diary 연관된 모든 page 불러오기

        return ResponseEntity.ok(List.of(new AddPageResponse()));
    }

    @Operation(summary = "Delete Page API", description = "페이지를 삭제한다.</br>(우선 사용되지 않을 API)</br>사용된다면 관리자 페이지에서 사용될 듯 함")
    @DeleteMapping("/page/{pageId}")
    public ResponseEntity<Void> delete(@PathVariable Long pageId) {

        // pageId에 해당하는 page 삭제
        pageService.deleteById(pageId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
