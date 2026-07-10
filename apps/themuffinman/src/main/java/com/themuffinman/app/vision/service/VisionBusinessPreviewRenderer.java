package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessOfferingResponseDTO;
import com.themuffinman.app.business.dto.BusinessBookingListResponseDTO;
import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.dto.BusinessOwnerDashboardDTO;
import com.themuffinman.app.business.dto.BusinessOwnerScheduleItemDTO;
import com.themuffinman.app.business.dto.BusinessOwnerScheduleSummaryDTO;
import com.themuffinman.app.business.dto.BusinessPublicPageDTO;
import com.themuffinman.app.business.dto.BusinessGalleryImageResponseDTO;
import com.themuffinman.app.business.service.BusinessBookingReadService;
import com.themuffinman.app.business.service.BusinessOwnerDashboardReadService;
import com.themuffinman.app.business.service.BusinessPublicReadService;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
class VisionBusinessPreviewRenderer {

    private static final DateTimeFormatter BUSINESS_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final BusinessPublicReadService businessPublicReadService;
    private final BusinessOwnerDashboardReadService businessOwnerDashboardReadService;
    private final BusinessBookingReadService businessBookingReadService;

    VisionCapabilityPreviewDTO previewBusiness(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        BusinessOwnerDashboardDTO dashboard = businessOwnerDashboardReadService.getMyDashboard(currentUser);
        BusinessPublicPageDTO page = businessPublicReadService.getPublicBusinessPage(dashboard.getSlug());
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "business_name", "Business name", page.getBusinessName());
        VisionCapabilityPreviewSupport.addItem(items, "business_slug", "Slug", page.getSlug());
        VisionCapabilityPreviewSupport.addItem(items, "business_headline", "Headline", page.getHeadline());
        VisionCapabilityPreviewSupport.addItem(items, "business_description", "Description", page.getDescription());
        VisionCapabilityPreviewSupport.addItem(items, "business_address", "Address", page.getPublicAddressLabel());
        VisionCapabilityPreviewSupport.addItem(items, "business_timezone", "Timezone", page.getTimezone());
        VisionCapabilityPreviewSupport.addItem(items, "business_booking_enabled", "Booking enabled", booleanLabel(page.isBookingEnabled()));
        VisionCapabilityPreviewSupport.addItem(items, "business_contact_email", "Email", page.getContactEmail());
        VisionCapabilityPreviewSupport.addItem(items, "business_contact_phone", "Phone", page.getContactPhone());
        VisionCapabilityPreviewSupport.addItem(items, "business_contact_whatsapp", "WhatsApp", page.getContactWhatsapp());
        VisionCapabilityPreviewSupport.addItem(items, "business_website_url", "Website", page.getWebsiteUrl());
        VisionCapabilityPreviewSupport.addItem(items, "business_offerings_count", "Offerings", String.valueOf(sizeOf(page.getOfferings())));
        VisionCapabilityPreviewSupport.addItem(items, "business_gallery_count", "Gallery images", String.valueOf(sizeOf(page.getGalleryImages())));

        addOfferings(items, page.getOfferings());
        addGalleryImages(items, page.getGalleryImages());

        String summary = page.getBusinessName() + " with " + sizeOf(page.getOfferings()) + " offering"
                + (sizeOf(page.getOfferings()) == 1 ? "" : "s")
                + " and " + sizeOf(page.getGalleryImages()) + " gallery image"
                + (sizeOf(page.getGalleryImages()) == 1 ? "" : "s") + ".";

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_business")
                .title("Business")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewBusinessAvailability(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        BusinessOwnerDashboardDTO dashboard = businessOwnerDashboardReadService.getMyDashboard(currentUser);
        BusinessOwnerScheduleSummaryDTO schedule = dashboard.getScheduleSummary();
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "business_name", "Business name", dashboard.getBusinessName());
        VisionCapabilityPreviewSupport.addItem(items, "business_slug", "Slug", dashboard.getSlug());
        VisionCapabilityPreviewSupport.addItem(items, "booking_enabled", "Booking enabled", booleanLabel(dashboard.isBookingEnabled()));
        VisionCapabilityPreviewSupport.addItem(items, "active_offering_count", "Active offerings", String.valueOf(dashboard.getActiveOfferingCount()));
        VisionCapabilityPreviewSupport.addItem(items, "pending_confirmation_count", "Pending confirmations", String.valueOf(dashboard.getPendingConfirmationCount()));
        VisionCapabilityPreviewSupport.addItem(items, "today_count", "Today", String.valueOf(dashboard.getTodayCount()));
        VisionCapabilityPreviewSupport.addItem(items, "upcoming_count", "Upcoming", String.valueOf(dashboard.getUpcomingCount()));
        VisionCapabilityPreviewSupport.addItem(items, "stale_threshold_minutes", "Stale threshold", String.valueOf(dashboard.getStaleThresholdMinutes()));
        VisionCapabilityPreviewSupport.addItem(items, "schedule_timezone", "Timezone", schedule == null ? null : schedule.getTimezone());
        VisionCapabilityPreviewSupport.addItem(items, "next_items_count", "Next items", String.valueOf(schedule == null || schedule.getNextItems() == null ? 0 : schedule.getNextItems().size()));

        addScheduleItems(items, schedule);

        String summary = dashboard.getBusinessName()
                + " schedule: "
                + dashboard.getTodayCount() + " today, "
                + dashboard.getUpcomingCount() + " upcoming, "
                + dashboard.getPendingConfirmationCount() + " pending.";

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_business_availability")
                .title("Business availability")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewBusinessBookings(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        BusinessOwnerDashboardDTO dashboard = businessOwnerDashboardReadService.getMyDashboard(currentUser);
        BusinessOwnerScheduleSummaryDTO schedule = dashboard == null ? null : dashboard.getScheduleSummary();
        BusinessBookingListResponseDTO bookings = businessBookingReadService.getOwnerBookings(null, currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "business_name", "Business name",
                dashboard == null ? null : dashboard.getBusinessName());
        VisionCapabilityPreviewSupport.addItem(items, "booking_enabled", "Booking enabled",
                dashboard == null ? null : booleanLabel(dashboard.isBookingEnabled()));
        VisionCapabilityPreviewSupport.addItem(items, "pending_confirmation_count", "Pending confirmations",
                dashboard == null ? null : String.valueOf(dashboard.getPendingConfirmationCount()));
        VisionCapabilityPreviewSupport.addItem(items, "today_count", "Today",
                dashboard == null ? null : String.valueOf(dashboard.getTodayCount()));
        VisionCapabilityPreviewSupport.addItem(items, "upcoming_count", "Upcoming",
                dashboard == null ? null : String.valueOf(dashboard.getUpcomingCount()));
        VisionCapabilityPreviewSupport.addItem(items, "schedule_timezone", "Timezone",
                schedule == null ? null : schedule.getTimezone());
        VisionCapabilityPreviewSupport.addItem(items, "bookings_total", "Bookings", String.valueOf(bookings.getTotalItems()));
        VisionCapabilityPreviewSupport.addItem(items, "bookings_page", "Page", bookings.getPage() + " of " + Math.max(bookings.getTotalPages(), 1));
        for (int index = 0; index < Math.min(bookings.getItems().size(), 5); index++) {
            BusinessBookingResponseDTO booking = bookings.getItems().get(index);
            if (booking == null) {
                continue;
            }
            VisionCapabilityPreviewSupport.addItem(
                    items,
                    "business_booking_" + booking.getId(),
                    booking.getCustomerUsername(),
                    formatBookingValue(booking)
            );
        }

        String summary = bookings.getItems().isEmpty()
                ? "No bookings found."
                : (dashboard == null
                ? bookings.getTotalItems() + " booking" + (bookings.getTotalItems() == 1 ? "" : "s") + " in the owner list."
                : dashboard.getBusinessName() + ": "
                + dashboard.getPendingConfirmationCount() + " pending, "
                + dashboard.getTodayCount() + " today, "
                + dashboard.getUpcomingCount() + " upcoming.");

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_business_bookings")
                .title("Business bookings")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    private void addOfferings(List<VisionSlotSummaryDTO> items, List<BusinessOfferingResponseDTO> offerings) {
        if (offerings == null) {
            return;
        }

        for (int index = 0; index < Math.min(offerings.size(), 3); index++) {
            BusinessOfferingResponseDTO offering = offerings.get(index);
            if (offering == null) {
                continue;
            }
            VisionCapabilityPreviewSupport.addItem(
                    items,
                    "business_offering_" + offering.getId(),
                    offering.getTitle(),
                    formatOfferingValue(offering)
            );
        }
    }

    private void addGalleryImages(List<VisionSlotSummaryDTO> items, List<BusinessGalleryImageResponseDTO> galleryImages) {
        if (galleryImages == null) {
            return;
        }

        for (int index = 0; index < Math.min(galleryImages.size(), 3); index++) {
            BusinessGalleryImageResponseDTO image = galleryImages.get(index);
            if (image == null) {
                continue;
            }
            VisionCapabilityPreviewSupport.addItem(
                    items,
                    "business_gallery_" + image.getId(),
                    image.getAltText() == null || image.getAltText().isBlank() ? "Gallery image " + (index + 1) : image.getAltText(),
                    image.getImageUrl()
            );
        }
    }

    private void addScheduleItems(List<VisionSlotSummaryDTO> items, BusinessOwnerScheduleSummaryDTO schedule) {
        if (schedule == null || schedule.getNextItems() == null) {
            return;
        }

        for (int index = 0; index < Math.min(schedule.getNextItems().size(), 5); index++) {
            BusinessOwnerScheduleItemDTO item = schedule.getNextItems().get(index);
            if (item == null) {
                continue;
            }
            VisionCapabilityPreviewSupport.addItem(
                    items,
                    "business_booking_" + item.getBookingId(),
                    item.getCustomerUsername(),
                    formatScheduleValue(item, schedule.getTimezone())
            );
        }
    }

    private String formatOfferingValue(BusinessOfferingResponseDTO offering) {
        List<String> parts = new ArrayList<>();
        if (offering.getSummary() != null && !offering.getSummary().isBlank()) {
            parts.add(offering.getSummary().trim());
        }
        String price = formatPrice(offering);
        if (price != null) {
            parts.add(price);
        }
        String duration = formatDuration(offering);
        if (duration != null) {
            parts.add(duration);
        }
        String capacity = formatCapacity(offering);
        if (capacity != null) {
            parts.add(capacity);
        }
        if (offering.isRequiresOwnerConfirmation()) {
            parts.add("owner confirmation");
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }

    private String formatPrice(BusinessOfferingResponseDTO offering) {
        if (offering.getBasePriceAmount() == null) {
            return null;
        }
        String amount = offering.getBasePriceAmount().stripTrailingZeros().toPlainString();
        if (offering.getBasePriceCurrency() == null || offering.getBasePriceCurrency().isBlank()) {
            return amount;
        }
        return amount + " " + offering.getBasePriceCurrency();
    }

    private String formatDuration(BusinessOfferingResponseDTO offering) {
        if (offering.getDurationMode() == null) {
            return null;
        }
        if (offering.getDefaultDurationMinutes() == null) {
            return offering.getDurationMode().name().toLowerCase();
        }
        return offering.getDefaultDurationMinutes() + " min";
    }

    private String formatCapacity(BusinessOfferingResponseDTO offering) {
        if (offering.getSlotCapacity() == null) {
            return null;
        }
        return offering.getSlotCapacity() + " slots";
    }

    private String formatScheduleValue(BusinessOwnerScheduleItemDTO item, String timezone) {
        List<String> parts = new ArrayList<>();
        if (item.getBusinessOfferingTitle() != null && !item.getBusinessOfferingTitle().isBlank()) {
            parts.add(item.getBusinessOfferingTitle().trim());
        }
        if (item.getStartsAt() != null) {
            parts.add(formatInstant(item.getStartsAt(), timezone));
        }
        if (item.getEndsAt() != null) {
            parts.add(formatInstant(item.getEndsAt(), timezone));
        }
        if (item.getStatusLabel() != null && !item.getStatusLabel().isBlank()) {
            parts.add(item.getStatusLabel().trim());
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }

    private String formatBookingValue(BusinessBookingResponseDTO booking) {
        List<String> parts = new ArrayList<>();
        if (booking.getBusinessOfferingTitle() != null && !booking.getBusinessOfferingTitle().isBlank()) {
            parts.add(booking.getBusinessOfferingTitle().trim());
        }
        if (booking.getStatusLabel() != null && !booking.getStatusLabel().isBlank()) {
            parts.add(booking.getStatusLabel().trim());
        }
        if (booking.getStartsAt() != null) {
            parts.add(BUSINESS_TIME_FORMAT.withZone(ZoneId.of(booking.getTimezone() == null || booking.getTimezone().isBlank() ? "UTC" : booking.getTimezone())).format(booking.getStartsAt()));
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }

    private String formatInstant(java.time.Instant instant, String timezone) {
        if (instant == null) {
            return null;
        }
        ZoneId zoneId = TimeSupport.resolveZoneIdOrDefault(timezone, ZoneId.systemDefault());
        return BUSINESS_TIME_FORMAT.withZone(zoneId).format(instant);
    }

    private String booleanLabel(boolean value) {
        return value ? "Yes" : "No";
    }

    private int sizeOf(List<?> values) {
        return values == null ? 0 : values.size();
    }
}
