// Ensure the DOM is fully loaded before initializing the carousel
document.addEventListener('DOMContentLoaded', function() {
    var myCarousel = new bootstrap.Carousel(document.getElementById('heroCarousel'), {
        interval: 4000,
        ride: 'carousel'
    });
});

    $(document).ready(function(){
        // Set interval for carousel to slide every 4 seconds
        setInterval(function() {
            $('#testimonialCarousel').carousel('next');
        }, 1000);  
    });
