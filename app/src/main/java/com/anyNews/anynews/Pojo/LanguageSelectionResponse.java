package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LanguageSelectionResponse implements Serializable {

    public class Data implements Serializable
    {
        @SerializedName("Please let us know the reason")
        @Expose
        private String Pleaseletusknow;

        public String getPleaseletusknow() {
            return Pleaseletusknow;
        }

        public void setPleaseletusknow(String pleaseletusknow) {
            Pleaseletusknow = pleaseletusknow;
        }

        public String getExpInterests() {
            return ExpInterests;
        }

        public void setExpInterests(String expInterests) {
            ExpInterests = expInterests;
        }



        @SerializedName("select_state")
        @Expose
        private String select_state;

        @SerializedName("select_district")
        @Expose
        private String select_district;


        @SerializedName("Explore your interests")
        @Expose
        private String ExpInterests;

        @SerializedName("Reload")
        @Expose
        private String reload;

        @SerializedName("News")
        @Expose
        private String news;
        @SerializedName("Search")
        @Expose
        private String search;
        @SerializedName("Post News")
        @Expose
        private String postNews;
        @SerializedName("Book Mark")
        @Expose
        private String bookMark;
        @SerializedName("More")
        @Expose
        private String more;
        @SerializedName("Profile")
        @Expose
        private String profile;
        @SerializedName("Upload Photo")
        @Expose
        private String uploadPhoto;
        @SerializedName("Name")
        @Expose
        private String name;
        @SerializedName("Change language")
        @Expose
        private String changeLanguage;
        @SerializedName("Notifications")
        @Expose
        private String notifications;
        @SerializedName("My Post")
        @Expose
        private String myPost;
        @SerializedName("Share")
        @Expose
        private String share;
        @SerializedName("Share this app")
        @Expose
        private String shareThisApp;
        @SerializedName("Rate this app")
        @Expose
        private String rateThisApp;
        @SerializedName("Help")
        @Expose
        private String help;
        @SerializedName("Privacy")
        @Expose
        private String privacy;
        @SerializedName("Terms and conditions")
        @Expose
        private String termsAndConditions;
        @SerializedName("Settings")
        @Expose
        private String settings;
        @SerializedName("Location")
        @Expose
        private String location;
        @SerializedName("Allow location access")
        @Expose
        private String allowLocationAccess;
        @SerializedName("logout")
        @Expose
        private String logout;
        @SerializedName("select language")
        @Expose
        private String selectLanguage;
        @SerializedName("news headlines")
        @Expose
        private String newsHeadlines;
        @SerializedName("upload news photo")
        @Expose
        private String uploadNewsPhoto;
        @SerializedName("i agree to terms and conditions")
        @Expose
        private String iAgreeToTermsAndConditions;
        @SerializedName("upload")
        @Expose
        private String upload;
        @SerializedName("search categories")
        @Expose
        private String searchCategories;
        @SerializedName("article 370")
        @Expose
        private String article370;
        @SerializedName("assembly elections 2019")
        @Expose
        private String assemblyElections2019;
        @SerializedName("ayodhya verdict")
        @Expose
        private String ayodhyaVerdict;
        @SerializedName("business")
        @Expose
        private String business;
        @SerializedName("entertainment")
        @Expose
        private String entertainment;
        @SerializedName("movie")
        @Expose
        private String movie;
        @SerializedName("movies")
        @Expose
        private String movies;
        @SerializedName("politics")
        @Expose
        private String politics;
        @SerializedName("sports")
        @Expose
        private String sports;
        @SerializedName("startups")
        @Expose
        private String startups;
        @SerializedName("technolgy")
        @Expose
        private String technolgy;
        @SerializedName("are you sure you want to logout")
        @Expose
        private String areYouSureYouWantToLogout;
        @SerializedName("yes")
        @Expose
        private String yes;
        @SerializedName("no")
        @Expose
        private String no;
        @SerializedName("choose your preferred language")
        @Expose
        private String chooseYourPreferredLanguage;
        @SerializedName("please fill all the fields")
        @Expose
        private String pleaseFillAllTheFields;
        @SerializedName("news category")
        @Expose
        private String newsCategory;
        @SerializedName("Write Comments")
        @Expose
        private String writeComments;
        @SerializedName("Hello")
        @Expose
        private String hello;
        @SerializedName("Below are the texts further required")
        @Expose
        private String belowAreTheTextsFurtherRequired;
        @SerializedName("continue")
        @Expose
        private String _continue;
        @SerializedName("no categories")
        @Expose
        private String noCategories;
        @SerializedName("ImageUploaded")
        @Expose
        private String imageUploaded;
        @SerializedName("no bookmarks")
        @Expose
        private String noBookmarks;
        @SerializedName("Allow Notifications for promotions and offers")
        @Expose
        private String allowNotificationsForPromotionsAndOffers;
        @SerializedName("Report")
        @Expose
        private String report;
        @SerializedName("send")
        @Expose
        private String send;

        @SerializedName("news description")
        @Expose
        private String NewsDescription;

        public String getNewsDescription() {
            return NewsDescription;
        }

        public void setNewsDescription(String newsDescription) {
            NewsDescription = newsDescription;
        }

        private final static long serialVersionUID = 2364498810993023132L;

        public String getSelect_state() {
            return select_state;
        }

        public void setSelect_state(String reload) {
            this.select_state = select_state;
        }

        public String getSelect_district() {
            return select_district;
        }

        public void setSelect_district(String select_district) {
            this.select_district = select_district;
        }



        public String getReload() {
            return reload;
        }

        public void setReload(String reload) {
            this.reload = reload;
        }

        public String getNews() {
            return news;
        }

        public void setNews(String news) {
            this.news = news;
        }

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }

        public String getPostNews() {
            return postNews;
        }

        public void setPostNews(String postNews) {
            this.postNews = postNews;
        }

        public String getBookMark() {
            return bookMark;
        }

        public void setBookMark(String bookMark) {
            this.bookMark = bookMark;
        }

        public String getMore() {
            return more;
        }

        public void setMore(String more) {
            this.more = more;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public String getUploadPhoto() {
            return uploadPhoto;
        }

        public void setUploadPhoto(String uploadPhoto) {
            this.uploadPhoto = uploadPhoto;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getChangeLanguage() {
            return changeLanguage;
        }

        public void setChangeLanguage(String changeLanguage) {
            this.changeLanguage = changeLanguage;
        }

        public String getNotifications() {
            return notifications;
        }

        public void setNotifications(String notifications) {
            this.notifications = notifications;
        }

        public String getMyPost() {
            return myPost;
        }

        public void setMyPost(String myPost) {
            this.myPost = myPost;
        }

        public String getShare() {
            return share;
        }

        public void setShare(String share) {
            this.share = share;
        }

        public String getShareThisApp() {
            return shareThisApp;
        }

        public void setShareThisApp(String shareThisApp) {
            this.shareThisApp = shareThisApp;
        }

        public String getRateThisApp() {
            return rateThisApp;
        }

        public void setRateThisApp(String rateThisApp) {
            this.rateThisApp = rateThisApp;
        }

        public String getHelp() {
            return help;
        }

        public void setHelp(String help) {
            this.help = help;
        }

        public String getPrivacy() {
            return privacy;
        }

        public void setPrivacy(String privacy) {
            this.privacy = privacy;
        }

        public String getTermsAndConditions() {
            return termsAndConditions;
        }

        public void setTermsAndConditions(String termsAndConditions) {
            this.termsAndConditions = termsAndConditions;
        }

        public String getSettings() {
            return settings;
        }

        public void setSettings(String settings) {
            this.settings = settings;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getAllowLocationAccess() {
            return allowLocationAccess;
        }

        public void setAllowLocationAccess(String allowLocationAccess) {
            this.allowLocationAccess = allowLocationAccess;
        }

        public String getLogout() {
            return logout;
        }

        public void setLogout(String logout) {
            this.logout = logout;
        }

        public String getSelectLanguage() {
            return selectLanguage;
        }

        public void setSelectLanguage(String selectLanguage) {
            this.selectLanguage = selectLanguage;
        }

        public String getNewsHeadlines() {
            return newsHeadlines;
        }

        public void setNewsHeadlines(String newsHeadlines) {
            this.newsHeadlines = newsHeadlines;
        }

        public String getUploadNewsPhoto() {
            return uploadNewsPhoto;
        }

        public void setUploadNewsPhoto(String uploadNewsPhoto) {
            this.uploadNewsPhoto = uploadNewsPhoto;
        }

        public String getIAgreeToTermsAndConditions() {
            return iAgreeToTermsAndConditions;
        }

        public void setIAgreeToTermsAndConditions(String iAgreeToTermsAndConditions) {
            this.iAgreeToTermsAndConditions = iAgreeToTermsAndConditions;
        }

        public String getUpload() {
            return upload;
        }

        public void setUpload(String upload) {
            this.upload = upload;
        }

        public String getSearchCategories() {
            return searchCategories;
        }

        public void setSearchCategories(String searchCategories) {
            this.searchCategories = searchCategories;
        }

        public String getArticle370() {
            return article370;
        }

        public void setArticle370(String article370) {
            this.article370 = article370;
        }

        public String getAssemblyElections2019() {
            return assemblyElections2019;
        }

        public void setAssemblyElections2019(String assemblyElections2019) {
            this.assemblyElections2019 = assemblyElections2019;
        }

        public String getAyodhyaVerdict() {
            return ayodhyaVerdict;
        }

        public void setAyodhyaVerdict(String ayodhyaVerdict) {
            this.ayodhyaVerdict = ayodhyaVerdict;
        }

        public String getBusiness() {
            return business;
        }

        public void setBusiness(String business) {
            this.business = business;
        }

        public String getEntertainment() {
            return entertainment;
        }

        public void setEntertainment(String entertainment) {
            this.entertainment = entertainment;
        }

        public String getMovie() {
            return movie;
        }

        public void setMovie(String movie) {
            this.movie = movie;
        }

        public String getMovies() {
            return movies;
        }

        public void setMovies(String movies) {
            this.movies = movies;
        }

        public String getPolitics() {
            return politics;
        }

        public void setPolitics(String politics) {
            this.politics = politics;
        }

        public String getSports() {
            return sports;
        }

        public void setSports(String sports) {
            this.sports = sports;
        }

        public String getStartups() {
            return startups;
        }

        public void setStartups(String startups) {
            this.startups = startups;
        }

        public String getTechnolgy() {
            return technolgy;
        }

        public void setTechnolgy(String technolgy) {
            this.technolgy = technolgy;
        }

        public String getAreYouSureYouWantToLogout() {
            return areYouSureYouWantToLogout;
        }

        public void setAreYouSureYouWantToLogout(String areYouSureYouWantToLogout) {
            this.areYouSureYouWantToLogout = areYouSureYouWantToLogout;
        }

        public String getYes() {
            return yes;
        }

        public void setYes(String yes) {
            this.yes = yes;
        }

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public String getChooseYourPreferredLanguage() {
            return chooseYourPreferredLanguage;
        }

        public void setChooseYourPreferredLanguage(String chooseYourPreferredLanguage) {
            this.chooseYourPreferredLanguage = chooseYourPreferredLanguage;
        }

        public String getPleaseFillAllTheFields() {
            return pleaseFillAllTheFields;
        }

        public void setPleaseFillAllTheFields(String pleaseFillAllTheFields) {
            this.pleaseFillAllTheFields = pleaseFillAllTheFields;
        }

        public String getNewsCategory() {
            return newsCategory;
        }

        public void setNewsCategory(String newsCategory) {
            this.newsCategory = newsCategory;
        }

        public String getWriteComments() {
            return writeComments;
        }

        public void setWriteComments(String writeComments) {
            this.writeComments = writeComments;
        }

        public String getHello() {
            return hello;
        }

        public void setHello(String hello) {
            this.hello = hello;
        }

        public String getBelowAreTheTextsFurtherRequired() {
            return belowAreTheTextsFurtherRequired;
        }

        public void setBelowAreTheTextsFurtherRequired(String belowAreTheTextsFurtherRequired) {
            this.belowAreTheTextsFurtherRequired = belowAreTheTextsFurtherRequired;
        }

        public String getContinue() {
            return _continue;
        }

        public void setContinue(String _continue) {
            this._continue = _continue;
        }

        public String getNoCategories() {
            return noCategories;
        }

        public void setNoCategories(String noCategories) {
            this.noCategories = noCategories;
        }

        public String getImageUploaded() {
            return imageUploaded;
        }

        public void setImageUploaded(String imageUploaded) {
            this.imageUploaded = imageUploaded;
        }

        public String getNoBookmarks() {
            return noBookmarks;
        }

        public void setNoBookmarks(String noBookmarks) {
            this.noBookmarks = noBookmarks;
        }

        public String getAllowNotificationsForPromotionsAndOffers() {
            return allowNotificationsForPromotionsAndOffers;
        }

        public void setAllowNotificationsForPromotionsAndOffers(String allowNotificationsForPromotionsAndOffers) {
            this.allowNotificationsForPromotionsAndOffers = allowNotificationsForPromotionsAndOffers;
        }

        public String getReport() {
            return report;
        }

        public void setReport(String report) {
            this.report = report;
        }

        public String getSend() {
            return send;
        }

        public void setSend(String send) {
            this.send = send;
        }

    }

    public class Extras implements Serializable
    {
        @SerializedName("msg")
        @Expose
        private String msg;

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        @SerializedName("Status")
        @Expose
        private String status;
        @SerializedName("Data")
        @Expose
        private Data data;
        private final static long serialVersionUID = 463099323123077976L;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

    }

        @SerializedName("success")
        @Expose
        private Boolean success;
        @SerializedName("extras")
        @Expose
        private Extras extras;
        private final static long serialVersionUID = -5843050117614797004L;

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public Extras getExtras() {
            return extras;
        }

        public void setExtras(Extras extras) {
            this.extras = extras;
        }

}
