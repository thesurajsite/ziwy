package com.anonymous.ziwy.Screens.LoginSection.ViewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anonymous.ziwy.GenericModels.LoadingScreenState
import com.anonymous.ziwy.MainActivity
import com.anonymous.ziwy.Screens.LoginSection.Models.AddUserInfoRequestModel
import com.anonymous.ziwy.Screens.LoginSection.Models.LoginRequestModel
import com.anonymous.ziwy.Screens.LoginSection.Models.SuccessResponseForUserData
import com.anonymous.ziwy.Utilities.Retrofit.Repository
import com.anonymous.ziwy.Utilities.Retrofit.Resource
import com.anonymous.ziwy.Utilities.Utils
import com.anonymous.ziwy.Utilities.ZConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = Repository()

    private val _state = MutableStateFlow(LoginStore())
    val state: StateFlow<LoginStore> = _state

    fun clearStates() {
        _state.value = LoginStore()
    }

    fun sendLoginRequest(loginRequestModel: LoginRequestModel, context: Context) {
        (context as MainActivity).startLogin(loginRequestModel)
    }

    fun setLoginStatus(isLoading: Boolean, isLoginSuccess: Boolean, successResponse: String?) {
        _state.value = _state.value.copy(
            isLoading = isLoading,
            isLoginSuccess = isLoginSuccess
        )
        if (successResponse != null) {
            _state.value = _state.value.copy(
                message = successResponse
            )
        }
    }

    fun setImageUri(uri: Uri) {
        _state.value = _state.value.copy(
            imageUri = uri
        )
    }

    //getUserData
    fun getUserData(responseWithToken: String) {
        viewModelScope.launch {
            repository.getUserData(_state.value.phoneNumber.value, _state.value.countryCode.value)
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(
                                loadingScreenState = LoadingScreenState(
                                    isLoading = true,
                                    screen = ZConstants.GET_USER_DATA
                                )
                            )
                        }

                        is Resource.Success -> {
                            val getParsedResponse = resource.data?.body?.let {
                                Utils.parseApiResponseForUserDetails(
                                    it
                                )
                            }

                            when (getParsedResponse) {
                                is SuccessResponseForUserData -> {
                                    _state.value = _state.value.copy(
                                        loadingScreenState = LoadingScreenState(
                                            isLoading = false,
                                            screen = ZConstants.GET_USER_DATA
                                        ),
                                        userData = getParsedResponse,
                                        isLoginSuccess = true,
                                        isNewUser = false,
                                        token = responseWithToken
                                    )
                                }

                                else -> {
                                    _state.value = _state.value.copy(
                                        loadingScreenState = LoadingScreenState(
                                            isLoading = false,
                                            screen = ZConstants.GET_USER_DATA
                                        ),
                                        isNewUser = true,
                                        token = responseWithToken
                                    )
                                }
                            }
                        }

                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                loadingScreenState = LoadingScreenState(
                                    isLoading = false,
                                    screen = ZConstants.GET_USER_DATA
                                ),
                                message = resource.message
                            )
                        }
                    }
                }
        }
    }

    fun addUserInformation(addUserInfoRequestModel: AddUserInfoRequestModel) {
        viewModelScope.launch {
            repository.addUserInformation(addUserInfoRequestModel).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            loadingScreenState = LoadingScreenState(
                                isLoading = true,
                                screen = ZConstants.ADD_USER_INFO
                            )
                        )
                    }

                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            loadingScreenState = LoadingScreenState(
                                isLoading = false,
                                screen = ZConstants.ADD_USER_INFO
                            ),
                            isLoginSuccess = true,
                            userData = SuccessResponseForUserData(
                                userName = resource.data?.body?.user?.userName,
                                email = resource.data?.body?.user?.email,
                                primaryKey = resource.data?.body?.user?.primaryKey,
                                gender = resource.data?.body?.user?.gender,
                                ageGroup = resource.data?.body?.user?.ageGroup,
                                notificationId = resource.data?.body?.user?.notificationId,
                                creationTime = resource.data?.body?.user?.creationTime
                            ),
                            message = "User information added successfully"
                        )
                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            loadingScreenState = LoadingScreenState(
                                isLoading = false,
                                screen = ZConstants.ADD_USER_INFO
                            ),
                            message = resource.message
                        )
                    }
                }
            }
        }
    }

    fun getAppUpdateInfo() {
        viewModelScope.launch {
            repository.getAppUpdateInfo().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            loadingScreenState = LoadingScreenState(
                                isLoading = true,
                                screen = ZConstants.APP_UPDATE_INFO
                            )
                        )
                    }

                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            loadingScreenState = LoadingScreenState(
                                isLoading = false,
                                screen = ZConstants.APP_UPDATE_INFO
                            ),
                            appUpdateInfo = resource.data
                        )
                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            loadingScreenState = LoadingScreenState(
                                isLoading = false,
                                screen = ZConstants.APP_UPDATE_INFO
                            ),
                            message = resource.message
                        )
                    }
                }
            }
        }
    }
}
