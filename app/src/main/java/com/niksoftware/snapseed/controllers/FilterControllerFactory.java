package com.niksoftware.snapseed.controllers;

public class FilterControllerFactory {
    public static FilterController createFilterController(ControllerContext context, int filterType) {
        FilterController controller;
        switch (filterType) {
            case 1:
                controller = new EmptyFilterController();
                break;
            case 2:
                controller = new AutoCorrectController();
                break;
            case 3:
                controller = new UPointController();
                break;
            case 4:
                controller = new TuneImageController();
                break;
            case 5:
                controller = new StraightenController();
                break;
            case 6:
                controller = new CropController();
                break;
            case 7:
                controller = new BlackAndWhiteController();
                break;
            case 8:
                controller = new VintageController();
                break;
            case 9:
                controller = new DramaController();
                break;
            case 10:
                controller = new GrungeController();
                break;
            case 11:
                controller = new CenterFocusController();
                break;
            case 12:
                controller = new FramesController();
                break;
            case 13:
                controller = new DetailsController();
                break;
            case 14:
                controller = new TiltAndShiftController();
                break;
            case 16:
                controller = new RetroluxController();
                break;
            case 17:
                controller = new FixedFramesController();
                break;
            case 18:
                controller = new AutoEnhanceController();
                break;
            case 20:
                controller = new CropAndRotateController();
                break;
            case 100:
                controller = new Ambiance2Controller();
                break;
            case 200:
                controller = new FilmController();
                break;
            default:
                controller = new EmptyFilterController();
                break;
        }
        controller.init(context);
        return controller;
    }
}
