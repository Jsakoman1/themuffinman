package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessReviewRequestDTO;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.model.BusinessReview;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.business.repository.BusinessReviewRepository;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessReviewServiceTest {

    @Mock
    private BusinessBookingRepository businessBookingRepository;
    @Mock
    private BusinessProfileRepository businessProfileRepository;
    @Mock
    private BusinessReviewRepository businessReviewRepository;
    @InjectMocks
    private BusinessReviewService service;

    @Test
    void customerCanReviewTheirCompletedBooking() {
        AppUser customer = user(1L, "customer");
        BusinessBooking booking = completedBooking(customer, user(2L, "owner"));
        when(businessBookingRepository.findDetailedById(10L)).thenReturn(Optional.of(booking));
        when(businessReviewRepository.findByBusinessBookingIdAndReviewerId(10L, 1L)).thenReturn(Optional.empty());
        when(businessReviewRepository.save(any(BusinessReview.class))).thenAnswer(invocation -> {
            BusinessReview review = invocation.getArgument(0);
            review.setId(30L);
            return review;
        });

        var response = service.createOrUpdateReview(10L, request(5, "Excellent work"), customer);

        assertEquals(30L, response.getId());
        assertEquals("customer", response.getReviewerUsername());
        assertEquals("Haircut", response.getServiceTitle());
        ArgumentCaptor<BusinessReview> reviewCaptor = ArgumentCaptor.forClass(BusinessReview.class);
        verify(businessReviewRepository).save(reviewCaptor.capture());
        assertEquals((short) 5, reviewCaptor.getValue().getStars());
    }

    @Test
    void rejectsReviewBeforeBookingIsCompleted() {
        AppUser customer = user(1L, "customer");
        BusinessBooking booking = completedBooking(customer, user(2L, "owner"));
        booking.setStatus(BusinessBookingStatus.CONFIRMED);
        when(businessBookingRepository.findDetailedById(10L)).thenReturn(Optional.of(booking));

        assertThrows(RuntimeException.class, () -> service.createOrUpdateReview(10L, request(5, null), customer));
    }

    @Test
    void rejectsSomeoneOtherThanTheBookingCustomer() {
        AppUser customer = user(1L, "customer");
        when(businessBookingRepository.findDetailedById(10L))
                .thenReturn(Optional.of(completedBooking(customer, user(2L, "owner"))));

        assertThrows(RuntimeException.class, () -> service.createOrUpdateReview(10L, request(5, null), user(3L, "other")));
    }

    private BusinessReviewRequestDTO request(int stars, String comment) {
        BusinessReviewRequestDTO request = new BusinessReviewRequestDTO();
        request.setStars(stars);
        request.setComment(comment);
        return request;
    }

    private BusinessBooking completedBooking(AppUser customer, AppUser owner) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(20L);
        profile.setOwner(owner);
        BusinessBooking booking = new BusinessBooking();
        booking.setId(10L);
        booking.setBusinessProfile(profile);
        booking.setCustomerUser(customer);
        booking.setStatus(BusinessBookingStatus.COMPLETED);
        booking.setOfferingTitleSnapshot("Haircut");
        return booking;
    }

    private AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        return user;
    }
}
