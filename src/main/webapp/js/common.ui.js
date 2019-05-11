$(function () {
    //
    var $overlay = $('.mp-overlay'),
        $layerPopup = $('.mp-layer-popup'),
        $btnLayerPopupClose = $('.mp-btn2-close, .mp-btn3-text.close'),
        $lyFloor = $('.mp-ly-floor'),
        $lyImageView = $('.mp-item-image-view'),
        $lyPromotion = $('.mp-item-promotion'),
        $lyParcel = $('.mp-ly-parcel');

    function layerPopupShow() {
        $layerPopup.show();
        $overlay.show();
    }
    function layerPopupHide() {
        $layerPopup.hide();
        $overlay.hide();
    }
    function lyFloorInit() {
        $('.mp-btn2-zoom').on('click', function (e) {
            e.preventDefault();

            var $this = $(this),
                mapImageURL = $this.attr('href'),
                $mapImage = $lyFloor.find('.mp-ly-map img'),
                isMapImageURL = (mapImageURL !== '#') ? true : false;

            if (isMapImageURL === false) return;
            $mapImage.attr('src', mapImageURL);
            layerPopupShow();

            $mapImage.smoothZoom('Reset').smoothZoom({
                width: 878,
                height: 462,
                pan_BUTTONS_SHOW: "NO",
                pan_LIMIT_BOUNDARY: "NO",
                border_SIZE: 0,
                // image_url: mapImageURL,
                /******************************************
                 Enable Responsive settings below if needed.
                 Max width and height values are optional.
                 ******************************************/
                responsive: false,
                responsive_maintain_ratio: true
            });
        });
    }
    function lyImageViewInit() {
        $('.item-search-content .item-image a').on('click', function (e) {
            e.preventDefault();

            $overlay.show();
            $lyImageView.show();
        })
    }
    function lyPromotionInit() {
        $('.mp-btn2-zoom').on('click', function (e) {
            e.preventDefault();

            $overlay.show();
            $lyPromotion.show();
        });
    }
    function lyParcelInit() {
        $('.mp-table-type1 .left a').on('click', function (e) {
            e.preventDefault();

            layerPopupShow();
        });
    }

    $overlay.on('click', function () {
        layerPopupHide();
    });
    $btnLayerPopupClose.on('click', function (e) {
        e.preventDefault();
        layerPopupHide();
    });

    if ($lyFloor.get(0)) lyFloorInit();
    if ($lyImageView.get(0)) lyImageViewInit();
    if ($lyPromotion.get(0)) lyPromotionInit();
    if ($lyParcel.get(0)) lyParcelInit();
});