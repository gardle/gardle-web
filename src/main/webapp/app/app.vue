<template>
    <v-app>
        <div style="position:fixed; top:0; left: 20px; z-index: 100" class="amber darken-2 white--text caption">
            Testprojekt
        </div>
        <v-app-bar
            app
            clipped-left
            :elevate-on-scroll="true"
            color="white"
            :style="appBarClasses">
            <v-app-bar-nav-icon v-if="hasMainNavigation && $vuetify.breakpoint.mdAndDown" @click="drawer = !drawer" class="green--text"/>

            <router-link class="flex text-decoration-none" to="/">
                <v-img
                    class="d-inline-block ml-3 mr-1"
                    src="/content/images/logo.png"
                    lazy-src="/content/images/logo@0,1x.png"
                    max-height="30"
                    max-width="30"
                    contain
                ></v-img>
                <v-toolbar-title class="d-inline-block green--text"
                                 v-if="(!hasMainNavigation && $vuetify.breakpoint.xsOnly) || $vuetify.breakpoint.smAndUp">
                    gardle
                </v-toolbar-title>
            </router-link>
            <v-spacer/>

            <template v-if="$vuetify.breakpoint.smAndDown">
                <v-menu offset-y>
                    <template v-slot:activator="{ on }">
                        <v-btn icon class="mx-1" v-on="on">
                            <v-icon>
                                mdi-earth
                            </v-icon>
                        </v-btn>
                    </template>
                    <v-list>
                        <v-list-item-group mandatory :value="currentLang" active-class="info--text">
                            <v-list-item v-for="(lang, i) in languages"
                                         @click="changeLanguage(lang)" :key="i" :value="lang">
                                {{$t(`language.${lang}`)}}
                            </v-list-item>
                        </v-list-item-group>
                    </v-list>
                </v-menu>
                <notification-tray v-if="loggedIn"/>
                <v-menu offset-y>
                    <template v-slot:activator="{ on }">
                        <v-btn
                            icon
                            v-on="on"
                        >
                            <v-icon>
                                mdi-dots-vertical
                            </v-icon>
                        </v-btn>
                    </template>
                    <v-list>
                        <template v-if="loggedIn">
                            <v-list-item to="/account" active-class="blue--text text--lighten-1">
                                <v-list-item-title v-text="$t('app.navigation.account')">
                                    Account
                                </v-list-item-title>
                            </v-list-item>
                            <v-list-item @click="handleLogoutClick">
                                <v-list-item-title v-text="$t('app.navigation.logout')">
                                    Logout
                                </v-list-item-title>
                            </v-list-item>
                            <v-divider/>
                        </template>
                        <template v-else>
                            <v-list-item @click="handleLoginShow">
                                <v-list-item-title v-text="$t('app.navigation.signIn')">
                                    Login
                                </v-list-item-title>
                            </v-list-item>
                            <v-list-item @click="handleSignUpClick">
                                <v-list-item-title v-text="$t('app.navigation.signUp')">
                                    Register
                                </v-list-item-title>
                            </v-list-item>
                        </template>
                    </v-list>
                </v-menu>
            </template>
            <template v-else>
                <v-menu offset-y>
                    <template v-slot:activator="{ on }">
                        <v-btn icon class="mx-2" v-on="on">
                            <v-icon>
                                mdi-earth
                            </v-icon>
                        </v-btn>
                    </template>
                    <v-list>
                        <v-list-item-group mandatory :value="currentLang" active-class="info--text">
                            <v-list-item v-for="(lang, i) in languages"
                                         @click="changeLanguage(lang)" :key="i" :value="lang">
                                {{$t(`language.${lang}`)}}
                            </v-list-item>
                        </v-list-item-group>
                    </v-list>
                </v-menu>
                <template v-if="loggedIn">
                    <notification-tray/>
                    <v-btn text class="blue--text text--lighten-1 mr-2" to="/account" v-text="$t('app.navigation.account')">
                        Account
                    </v-btn>
                    <v-btn text class="grey--text text--lighten-1" @click="handleLogoutClick" v-text="$t('app.navigation.logout')">
                        Logout
                    </v-btn>
                </template>
                <template v-else>
                    <v-btn text
                           class="green--text text--lighten-1 mr-2"
                           @click="handleLoginShow" v-text="$t('app.navigation.signIn')">
                        Login
                    </v-btn>

                    <v-btn outlined class="grey--text text--lighten-1"
                           @click="handleSignUpClick" v-text="$t('app.navigation.signUp')">
                        Register
                    </v-btn>
                </template>
            </template>
        </v-app-bar>

        <v-navigation-drawer app
                             clipped
                             left
                             v-if="hasMainNavigation"
                             v-model="drawer">
            <template v-slot:default>
                <portal-target name="navigationDrawerContent">
                    Some Default content
                </portal-target>
            </template>
            <template v-slot:append>
                <portal-target name="navigationDrawerAppend">
                </portal-target>
            </template>
        </v-navigation-drawer>

        <v-content class="transparent">
            <v-container
                fluid
                class="fill-height white align-content-start pa-0 ma-0"
            >
                <router-view/>
            </v-container>
        </v-content>

        <v-dialog v-model="loginModal" max-width="400" :fullscreen="$vuetify.breakpoint.xsOnly">
            <v-card>
                <v-card-title>
                    <h3 class="headline d-inline-block" v-text="$t('login.title')">Login</h3>
                    <v-spacer/>
                    <v-btn icon @click="handleLoginHide">
                        <v-icon>
                            mdi-close
                        </v-icon>
                    </v-btn>
                </v-card-title>
                <v-card-text>
                    <login-form></login-form>
                </v-card-text>
            </v-card>
        </v-dialog>
        <v-snackbar v-model="notifications"
                    :color="notificationColor"
                    :timeout="notificationTimeout"
                    :multi-line="notificationMultiline"
                    bottom left>
            <v-flex xs10 v-html="notificationText"></v-flex>
            <v-icon v-if="notificationPrimaryAction !== null"
                    :class="[notificationColor+'--text', `text--${(notificationClose) ? 'lighten-1' : 'darken-2'} px-1`]"
                    @click="notificationPrimaryAction.callback">
                {{notificationPrimaryAction.icon}}
            </v-icon>
            <v-icon v-if="notificationAction !== null"
                    :class="[notificationColor+'--text', `text--${(notificationClose) ? 'lighten-1' : 'darken-2'}`]"
                    @click="notificationAction.callback">
                {{notificationAction.icon}}
            </v-icon>
        </v-snackbar>
    </v-app>
</template>

<script lang="ts" src="./app.component.ts">
</script>

<style>
    .row {
        margin-left: 0px !important;
        margin-right: 0px !important;
    }

    .relative {
        position: relative;
    }

    .absolute {
        position: absolute;
    }

    .text-decoration-none {
        text-decoration: none;
    }

    .cursor-pointer {
        cursor: pointer;
    }
</style>
