package com.kosta.geekku.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.kosta.geekku.dto.InteriorDto;
import com.kosta.geekku.dto.InteriorRequestDto;
import com.kosta.geekku.dto.ReviewDto;
import com.kosta.geekku.dto.SampleDto;
import com.kosta.geekku.entity.Interior;
import com.kosta.geekku.entity.InteriorBookmark;
import com.kosta.geekku.entity.InteriorRequest;
import com.kosta.geekku.entity.InteriorReview;
import com.kosta.geekku.entity.InteriorSample;
import com.kosta.geekku.entity.User;
import com.kosta.geekku.repository.InteriorBookmarkRepository;
import com.kosta.geekku.repository.InteriorDslRepository;
import com.kosta.geekku.repository.InteriorRepository;
import com.kosta.geekku.repository.InteriorRequestRepository;
import com.kosta.geekku.repository.InteriorReviewRepository;
import com.kosta.geekku.repository.InteriorSampleRepository;
import com.kosta.geekku.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InteriorSeviceImpl implements InteriorService {

	private final InteriorRepository interiorRepository;
	private final InteriorDslRepository interiorDslRepository;
	private final InteriorBookmarkRepository interiorBookmarkRepository;
	private final UserRepository userRepository;
	private final InteriorSampleRepository interiorSampleRepository;
	private final InteriorReviewRepository interiorReviewRepository;
	private final InteriorRequestRepository interiorRequestRepository;
	
	@Value("${upload.path}")
	private String uploadPath;

	@Override
	public List<InteriorDto> interiorListForMain() throws Exception {
		List<InteriorDto> interiorDtoList = null;
		interiorDtoList = interiorDslRepository.findInteriorListForMain().stream().map(i -> i.toDto())
				.collect(Collectors.toList());
		return interiorDtoList;
	}

	@Override
	public List<SampleDto> sampleListForMain() throws Exception {
		List<SampleDto> sampleList = null;
		sampleList = interiorDslRepository.findSampleListForMain().stream().map(s->s.toDto()).collect(Collectors.toList());
		return sampleList;
	}

	@Override
	public List<InteriorDto> interiorList(String possibleLocation) throws Exception {
		List<InteriorDto> interiorDtoList = null;
		Long allCnt = 0L;
		if (possibleLocation.equals("전체")) {
			interiorDtoList = interiorDslRepository.interiorListAll().stream().map(i -> i.toDto())
					.collect(Collectors.toList());
			allCnt = interiorDslRepository.interiorCountAll();
		} else {
			interiorDtoList = interiorDslRepository.interiorListByLoc(possibleLocation).stream().map(i -> i.toDto())
					.collect(Collectors.toList());
			allCnt = interiorDslRepository.interiorCountByLoc(possibleLocation);
		}
		return interiorDtoList;
	}

	@Override
	public Integer checkBookmark(String userId, Integer interiorNum) throws Exception {
		return interiorDslRepository.findInteriorBookmark(UUID.fromString(userId), interiorNum);
	}

	@Override
	@Transactional
	public boolean toggleBookmark(String userId, Integer interiorNum) throws Exception {
		InteriorBookmark interiorBookmark = interiorBookmarkRepository.findByInteriorNumAndUserId(interiorNum,
				UUID.fromString(userId));
		System.out.println(interiorBookmark);

		if (interiorBookmark == null) {
			interiorBookmarkRepository
					.save(InteriorBookmark.builder().userId(UUID.fromString(userId)).interiorNum(interiorNum).build());
			return true;
		} else {
			interiorBookmarkRepository.deleteById(interiorBookmark.getBookmarkInteriorNum());
			return false;
		}
	}

	@Override
	public Integer interiorRegister(InteriorDto interiorDto) throws Exception {
		Interior interior = interiorDto.toEntity();
		interiorRepository.save(interior);
		return interior.getInteriorNum();
	}

	@Override
	public InteriorDto interiorCompanyDetail(Integer num) throws Exception {
		Interior interior = interiorRepository.findById(num).orElseThrow(() -> new Exception("글번호 오류"));
		System.out.println("service" + num);
		// onestopDslRepository.updateOnestopViewCount(num, onestop.getViewCount() + 1);
		return interior.toDto();
	}

	public Integer sampleRegister(SampleDto sampleDto) throws Exception {
		InteriorSample sample = sampleDto.toEntity();
		interiorSampleRepository.save(sample);
//		if(sampleDto.getInteriorNum() ==  )	//사례 인테리어번호와 작성자 인테리어번호가 같을경우만 작성
		return sample.getSampleNum();
	}

	@Override
	public Integer reviewRegister(ReviewDto reviewDto) throws Exception {
		InteriorReview review = reviewDto.toEntity();
		interiorReviewRepository.save(review);
		return review.getReviewNum();
	}

	@Override
	public SampleDto sampleDetail(Integer num) throws Exception {
		InteriorSample sample = interiorSampleRepository.findById(num).orElseThrow(()->new Exception("글 번호 오류"));
		return sample.toDto();
	}

	@Override
	public Integer interiorRequest(InteriorRequestDto requestDto) throws Exception {
		InteriorRequest request = requestDto.toEntity();
		interiorRequestRepository.save(request);
		return request.getRequestNum();
	}

	@Override
	public InteriorRequestDto requestDetail(Integer num) throws Exception {
		InteriorRequest request = interiorRequestRepository.findById(num).orElseThrow(()->new Exception("요청 글 번호 오류"));
		return request.toDto();
	}

	@Override
	public List<InteriorSample> sampleList(String date, String type, String style, Integer size, String location)
			throws Exception {
		List<InteriorSample> sampleDtoList = null;
		Long allCnt = 0L;
		sampleDtoList = interiorDslRepository.sampleListByFilter(date, type, style, size, location);
		allCnt = interiorDslRepository.sampleCountByFilter(date, type, style, size, location);
		return sampleDtoList;
	}

	@Override
	public Map<String, Object> interiorDetail(Integer interiorNum) throws Exception {
		Map<String,Object> detailInfo = new HashMap<>();
		Interior interiorDetail = interiorRepository.findById(interiorNum).orElseThrow(()->new Exception("인테리어 업체 번호 오류"));
		List<InteriorSample> sampleDetail = interiorSampleRepository.findByInterior_InteriorNum(interiorNum);
		List<InteriorReview> reviewDetail = interiorReviewRepository.findByInterior_interiorNum(interiorNum);
		
		InteriorDto interiorInfo = interiorDetail.toDto();
		List<SampleDto> sampleInfo = sampleDetail.stream().map(s->s.toDto()).collect(Collectors.toList());
		List<ReviewDto> reviewInfo = reviewDetail.stream().map(r->r.toDto()).collect(Collectors.toList());				
		
		detailInfo.put("interiorDetail", interiorInfo);
		detailInfo.put("sampleDetail", sampleInfo);
		detailInfo.put("reviewDetail", reviewInfo);
		return detailInfo;
	}



	public Page<InteriorRequestDto> interiorRequestListForUserMypage(int page, int size, String userId)
			throws Exception {
		Optional<User> user = userRepository.findById(UUID.fromString(userId));
		
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<InteriorRequestDto> pageInfo = interiorRequestRepository.findAllByUser(user, pageable).map(InteriorRequest::toDto);
		
		return pageInfo;
	}

	@Override
	public Page<ReviewDto> reviewListForUserMypage(int page, int size, String userId) throws Exception {
		Optional<User> user = userRepository.findById(UUID.fromString(userId));
		
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<ReviewDto> pageInfo = interiorReviewRepository.findAllByUser(user, pageable).map(InteriorReview::toDto);
		
		return pageInfo;
	}

	@Override
	public void updateReview(ReviewDto reviewDto, Integer num) throws Exception {
		InteriorReview review = interiorReviewRepository.findById(num).orElseThrow(() -> new Exception("리뷰 글번호 오류"));

		review.setContent(reviewDto.getContent());
		// 이미지 수정 필요함
		interiorReviewRepository.save(review);
	}

	@Override
	public void deleteReview(Integer num) throws Exception {
		InteriorReview review = interiorReviewRepository.findById(num).orElseThrow(() -> new Exception("리뷰 글번호 오류"));
		interiorReviewRepository.deleteById(num);
	}	


}
